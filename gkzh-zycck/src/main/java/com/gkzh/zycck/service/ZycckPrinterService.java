package com.gkzh.zycck.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.common.utils.DateUtils;
import com.gkzh.zycck.domain.ZycckPrinter;
import com.gkzh.zycck.mapper.ZycckPrinterMapper;
import com.hprt.domain.HPRTPrinter;
import com.hprt.domain.HPRTResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 未来职业猜猜看打印机业务。
 *
 * <p>汉印设备以 equipment_sn 作为云端唯一标识，本地 printer_id 仅作为业务关联标识。</p>
 */
@Service
public class ZycckPrinterService {
    public static final String ENABLED = "0";
    public static final String DISABLED = "1";
    public static final String BOUND = "0";
    public static final String UNBOUND = "1";

    private final ZycckPrinterMapper printerMapper;
    private final HprtCloudService hprtCloudService;

    public ZycckPrinterService(ZycckPrinterMapper printerMapper, HprtCloudService hprtCloudService) {
        this.printerMapper = printerMapper;
        this.hprtCloudService = hprtCloudService;
    }

    public List<ZycckPrinter> listEnabledPrinters() {
        return printerMapper.selectList(new QueryWrapper<ZycckPrinter>()
                .eq("enabled", ENABLED)
                .eq("bound_status", BOUND)
                .orderByAsc("printer_name")
                .orderByAsc("printer_id"));
    }

    public List<ZycckPrinter> listAllPrinters() {
        return printerMapper.selectList(new QueryWrapper<ZycckPrinter>()
                .orderByAsc("enabled")
                .orderByAsc("printer_name")
                .orderByAsc("printer_id"));
    }

    public ZycckPrinter findById(Long printerId) {
        if (printerId == null) {
            throw new ServiceException("缺少打印机编号");
        }
        ZycckPrinter printer = printerMapper.selectById(printerId);
        if (printer == null) {
            throw new ServiceException("打印机不存在");
        }
        return printer;
    }

    /**
     * 从汉印云端分页拉取已绑定设备，并按 equipment_sn 同步到本地。
     */
    @Transactional
    public List<ZycckPrinter> syncCloudPrinters() {
        List<ZycckPrinter> synced = new ArrayList<>();
        int page = 1;
        int size = 100;
        while (true) {
            HPRTResult<List<HPRTPrinter>> result = hprtCloudService.pagePrinter(page, size);
            ensureSuccess(result, "查询汉印打印机列表失败");
            List<HPRTPrinter> cloudPrinters = result.getData();
            if (cloudPrinters == null || cloudPrinters.isEmpty()) {
                break;
            }
            for (HPRTPrinter cloudPrinter : cloudPrinters) {
                if (cloudPrinter == null || !StringUtils.hasText(cloudPrinter.getEquipment_sn())) {
                    continue;
                }
                synced.add(upsertCloudPrinter(cloudPrinter));
            }
            if (cloudPrinters.size() < size) {
                break;
            }
            page++;
        }
        return synced;
    }

    /**
     * 将一个设备绑定到汉印云端，并同步本地记录。
     */
    @Transactional
    public ZycckPrinter bindPrinter(String equipmentSn, String equipmentSecret, String printerName) {
        requireText(equipmentSn, "设备序列号不能为空");
        HPRTPrinter cloudPrinter = HPRTPrinter.builder()
                .equipment_sn(equipmentSn.trim())
                .equipment_secret(equipmentSecret == null ? "" : equipmentSecret.trim())
                .build();
        HPRTResult<Void> result = hprtCloudService.addPrinter(List.of(cloudPrinter));
        ensureSuccess(result, "绑定汉印打印机失败");

        ZycckPrinter local = printerMapper.selectOne(new QueryWrapper<ZycckPrinter>()
                .eq("equipment_sn", equipmentSn.trim()));
        Date now = DateUtils.getNowDate();
        if (local == null) {
            local = new ZycckPrinter();
            local.setEquipmentSn(equipmentSn.trim());
            local.setCreateTime(now);
        }
        if (StringUtils.hasText(printerName)) {
            local.setPrinterName(printerName.trim());
        } else if (!StringUtils.hasText(local.getPrinterName())) {
            local.setPrinterName(equipmentSn.trim());
        }
        local.setEquipmentSecret(cloudPrinter.getEquipment_secret());
        local.setEnabled(ENABLED);
        local.setBoundStatus(BOUND);
        local.setLastSyncTime(now);
        local.setUpdateTime(now);
        if (local.getPrinterId() == null) {
            printerMapper.insert(local);
        } else {
            printerMapper.updateById(local);
        }
        return local;
    }

    /**
     * 解绑云端设备，但保留本地记录和历史任务，避免打印历史丢失。
     */
    @Transactional
    public ZycckPrinter unbindPrinter(Long printerId) {
        ZycckPrinter printer = findById(printerId);
        if (BOUND.equals(printer.getBoundStatus())) {
            HPRTResult<Void> result = hprtCloudService.unbindPrinter(List.of(printer.getEquipmentSn()));
            ensureSuccess(result, "解绑汉印打印机失败");
        }
        printer.setBoundStatus(UNBOUND);
        printer.setEnabled(DISABLED);
        printer.setUpdateTime(DateUtils.getNowDate());
        printerMapper.updateById(printer);
        return printer;
    }

    /**
     * 查询本地打印机的实时状态并更新缓存。
     */
    @Transactional
    public ZycckPrinter refreshPrinterStatus(Long printerId) {
        ZycckPrinter printer = findById(printerId);
        HPRTResult<List<HPRTPrinter>> result = hprtCloudService.queryPrinterStatus(List.of(printer.getEquipmentSn()));
        ensureSuccess(result, "查询汉印打印机状态失败");
        List<HPRTPrinter> statuses = result.getData();
        if (statuses != null && !statuses.isEmpty() && statuses.get(0) != null) {
            printer.setStatus(statuses.get(0).getStatus());
        }
        Date now = DateUtils.getNowDate();
        printer.setLastStatusSyncTime(now);
        printer.setUpdateTime(now);
        printerMapper.updateById(printer);
        return printer;
    }

    private ZycckPrinter upsertCloudPrinter(HPRTPrinter cloudPrinter) {
        String equipmentSn = cloudPrinter.getEquipment_sn().trim();
        ZycckPrinter local = printerMapper.selectOne(new QueryWrapper<ZycckPrinter>()
                .eq("equipment_sn", equipmentSn));
        Date now = DateUtils.getNowDate();
        if (local == null) {
            local = new ZycckPrinter();
            local.setEquipmentSn(equipmentSn);
            local.setPrinterName(StringUtils.hasText(cloudPrinter.getName())
                    ? cloudPrinter.getName() : equipmentSn);
            local.setEnabled(ENABLED);
            local.setCreateTime(now);
        }
        if (StringUtils.hasText(cloudPrinter.getName())) {
            local.setCloudName(cloudPrinter.getName());
        }
        if (StringUtils.hasText(cloudPrinter.getModel_name())) {
            local.setModelName(cloudPrinter.getModel_name());
        }
        if (cloudPrinter.getStatus() != null) {
            local.setStatus(cloudPrinter.getStatus());
        }
        if (StringUtils.hasText(cloudPrinter.getEquipment_secret())) {
            local.setEquipmentSecret(cloudPrinter.getEquipment_secret());
        }
        local.setBoundStatus(BOUND);
        local.setLastSyncTime(now);
        local.setUpdateTime(now);
        if (local.getPrinterId() == null) {
            printerMapper.insert(local);
        } else {
            printerMapper.updateById(local);
        }
        return local;
    }

    private void ensureSuccess(HPRTResult<?> result, String defaultMessage) {
        if (result == null || !Boolean.TRUE.equals(result.getStatus())) {
            String message = result == null ? null : result.getMsg();
            throw new ServiceException(StringUtils.hasText(message) ? message : defaultMessage);
        }
    }

    private void requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new ServiceException(message);
        }
    }
}
