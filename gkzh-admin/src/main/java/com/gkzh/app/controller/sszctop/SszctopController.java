package com.gkzh.app.controller.sszctop;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gkzh.activity.domain.week.GkzhActivityGame;
import com.gkzh.activity.service.IActivityWeekService;
import com.gkzh.app.websocket.GameRoomSocketHandler;
import com.gkzh.common.core.controller.FrontBaseController;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.common.core.domain.model.StudentCheckin;
import com.gkzh.common.utils.QRCodeUtils;
import com.gkzh.sszctop.domain.*;
import com.gkzh.sszctop.mapper.*;
import com.gkzh.sszctop.service.SszctopRoomService;
import com.google.zxing.WriterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/** 学生端 API：仅组装参数和返回数据，职业规则全部由 sszctop 模块处理。 */
@RestController
@RequestMapping("/api/sszctop")
public class SszctopController extends FrontBaseController {
    @Autowired private SszctopRoomService service;
    @Autowired private IActivityWeekService weekService;
    @Autowired private SszctopCareerMapper careerMapper;
    @Autowired private SszctopDimensionMapper dimensionMapper;
    @Autowired private SszctopDimensionRankMapper rankMapper;
    @Autowired private SszctopStudentReportMapper reportMapper;
    @Autowired private GameRoomSocketHandler socketHandler;

    @GetMapping("/catalog") public AjaxResult catalog() { return AjaxResult.success(service.dimensions()); }
    /** 创建房间时校验活动实例，禁止使用其他活动的 gameId 创建本活动房间。 */
    @PostMapping("/rooms") public AjaxResult create(@RequestBody Map<String,Object> b) { Long instanceId=id(b.get("instanceId")); if(instanceId==null)return AjaxResult.error("缺少活动信息"); StudentCheckin s=getCurrentStudent(); SszctopRoom r=service.create(game(id(b.get("gameId")),instanceId),String.valueOf(b.getOrDefault("mode","solo")),s.getUserId(),s.getStuId(),s.getStuName(),s.getStuNo()); notifyRoom(r,"room.created"); return AjaxResult.success(view(r)); }
    /** 加入房间时同时校验二维码携带的活动实例和房间实际所属活动。 */
    @PostMapping("/rooms/join") public AjaxResult join(@RequestBody Map<String,Object> b) { Long instanceId=id(b.get("instanceId")); if(instanceId==null)return AjaxResult.error("缺少活动信息"); Long gameId=id(b.get("gameId")); game(gameId,instanceId); SszctopRoom existing=service.room(String.valueOf(b.get("roomCode"))); if(!instanceId.equals(existing.getInstanceId())||!gameId.equals(existing.getGameId()))return AjaxResult.error("房间不属于当前活动游戏"); StudentCheckin s=getCurrentStudent(); SszctopRoom r=service.join(existing.getRoomCode(),gameId,s.getUserId(),s.getStuId(),s.getStuName(),s.getStuNo()); notifyRoom(r,"member.joined"); return AjaxResult.success(view(r)); }
    @PostMapping("/rooms/{code}/leave") public AjaxResult leave(@PathVariable String code) { SszctopRoom r=service.leave(code,getCurrentStudent().getUserId()); notifyRoom(r,"member.left"); return AjaxResult.success(view(r)); }
    /** 开始游戏再次校验活动实例，避免页面残留参数或跨活动房间被启动。 */
    @PostMapping("/rooms/{code}/start") public AjaxResult start(@PathVariable String code,@RequestBody Map<String,Object> b) { Long instanceId=id(b.get("instanceId")); SszctopRoom existing=service.room(code); if(instanceId==null||!instanceId.equals(existing.getInstanceId()))return AjaxResult.error("房间不属于当前活动"); SszctopRoom r=service.start(code,getCurrentStudent().getUserId(),id(b.get("dimensionId"))); notifyRoom(r,"room.started"); return AjaxResult.success(view(r)); }
    @PostMapping("/rooms/{code}/ready") public AjaxResult ready(@PathVariable String code,@RequestBody Map<String,Object> b) { SszctopRoom r=service.ready(code,getCurrentStudent().getUserId(),Boolean.parseBoolean(String.valueOf(b.getOrDefault("ready",true)))); notifyRoom(r,"member.ready"); return AjaxResult.success(view(r)); }
    @PutMapping("/rooms/{code}/order") public AjaxResult order(@PathVariable String code,@RequestBody Map<String,Object> b) { SszctopRoom r=service.updateOrder(code,getCurrentStudent().getUserId(),ids(b.get("careerIds")),integer(b.get("orderVersion"))); notifyRoom(r,"order.updated"); return AjaxResult.success(view(r)); }
    @PostMapping("/rooms/{code}/confirm") public AjaxResult confirm(@PathVariable String code,@RequestBody Map<String,Object> b) { SszctopRoom r=service.confirm(code,getCurrentStudent().getUserId(),integer(b.get("orderVersion"))); notifyRoom(r,"passed".equals(r.getStatus())||"failed".equals(r.getStatus())?"room.finished":"confirm.updated"); return AjaxResult.success(view(r)); }
    @GetMapping("/rooms/{code}") public AjaxResult room(@PathVariable String code) { SszctopRoom r=service.room(code); SszctopRoomMember m=service.member(r.getRoomId(),getCurrentStudent().getUserId()); if(m==null||m.getRemovedTime()!=null)return AjaxResult.error("你不在当前房间"); return AjaxResult.success(view(r)); }
    /** 返回 data URL，避免小程序 image 请求无法携带登录请求头。 */
    /** 房间二维码必须同时携带活动实例、游戏和游戏类型，扫码时才能区分不同活动中的同类游戏。 */
    @GetMapping("/rooms/{code}/qrcode") public AjaxResult qrcode(@PathVariable String code) { try { SszctopRoom r=service.room(code); String content="GKZH_MP:/pages/sszctop/index?instanceId="+r.getInstanceId()+"&activityId="+r.getInstanceId()+"&gameId="+r.getGameId()+"&gameType=sszctop&roomCode="+r.getRoomCode(); byte[] bytes=QRCodeUtils.generateQRCode(content,360,360); return AjaxResult.success("data:image/png;base64,"+Base64.getEncoder().encodeToString(bytes)); } catch (WriterException|IOException e) { return AjaxResult.error("房间二维码生成失败"); } }
    @PostMapping("/rooms/{code}/socket-ticket") public AjaxResult socketTicket(@PathVariable String code) { StudentCheckin s=getCurrentStudent(); SszctopRoom r=service.room(code); SszctopRoomMember m=service.member(r.getRoomId(),s.getUserId()); if(m==null||m.getRemovedTime()!=null)return AjaxResult.error("你不在当前房间"); String channel="sszctop/"+r.getGameId()+"/"+r.getRoomCode(); return AjaxResult.success(Collections.singletonMap("ticket",socketHandler.ticket(channel,s.getUserId(),s.getStuName()))); }
    /** 返回个人报告及“维度排序及文案”中职业对应的排序说明快照。 */
    @GetMapping("/report/{gameId}") public AjaxResult report(@PathVariable Long gameId) { StudentCheckin s=getCurrentStudent(); SszctopStudentReport r=reportMapper.selectOne(new QueryWrapper<SszctopStudentReport>().eq("game_id",gameId).eq("user_id",s.getUserId()).orderByDesc("report_id").last("limit 1")); if(r==null)return AjaxResult.error("未找到个人报告"); r.setRankDetails(reportRankDetails(r)); return AjaxResult.success(r); }

    /**
     * 新报告读取结算时保存的排序说明快照；旧报告没有快照时，按其历史维度和职业补齐当前对应说明。
     * 两种情况都只返回本局展示的职业，避免把同一维度下未抽取的职业带入个人报告。
     */
    private List<SszctopDimensionRank> reportRankDetails(SszctopStudentReport report){
        try{
            JSONObject reportJson=JSON.parseObject(report.getReportJson());
            if(reportJson!=null&&reportJson.get("rankDetails")!=null){List<SszctopDimensionRank> snapshot=JSON.parseArray(JSON.toJSONString(reportJson.get("rankDetails")),SszctopDimensionRank.class);if(snapshot!=null&&!snapshot.isEmpty())return snapshot;}
            JSONObject dimension=JSON.parseObject(report.getDimensionSnapshot());
            List<SszctopCareer> careers=JSON.parseArray(report.getCareersSnapshot(),SszctopCareer.class);
            if(dimension==null||dimension.getLong("dimensionId")==null||careers==null||careers.isEmpty())return Collections.emptyList();
            List<Long> careerIds=careers.stream().map(SszctopCareer::getCareerId).filter(Objects::nonNull).collect(Collectors.toList());
            return careerIds.isEmpty()?Collections.emptyList():rankMapper.selectList(new QueryWrapper<SszctopDimensionRank>().eq("dimension_id",dimension.getLong("dimensionId")).in("career_id",careerIds).orderByAsc("rank_order"));
        }catch(Exception ignored){return Collections.emptyList();}
    }

    /** 按房间保存的随机职业顺序返回，不能直接使用数据库 IN 查询的默认顺序。 */
    private Map<String,Object> view(SszctopRoom r) { Map<String,Object> v=new LinkedHashMap<>(); v.put("currentUserId",getCurrentStudent().getUserId()); v.put("roomCode",r.getRoomCode()); v.put("gameId",r.getGameId()); v.put("mode",r.getMode()); v.put("status",r.getStatus()); v.put("ownerUserId",r.getOwnerUserId()); v.put("orderVersion",r.getOrderVersion()); v.put("members",service.active(r.getRoomId())); if(r.getDimensionId()!=null)v.put("dimension",dimensionMapper.selectById(r.getDimensionId())); List<Long> cs=ids(r.getCareerIds()); List<SszctopCareer> careers=cs.isEmpty()?Collections.emptyList():careerMapper.selectBatchIds(cs); Map<Long,SszctopCareer> careerMap=careers.stream().collect(Collectors.toMap(SszctopCareer::getCareerId,x->x)); v.put("careers",cs.stream().map(careerMap::get).filter(Objects::nonNull).collect(Collectors.toList())); v.put("sharedOrderIds",ids(r.getSharedOrderIds())); if("passed".equals(r.getStatus())||"failed".equals(r.getStatus())) { List<SszctopDimensionRank> ranks=service.ranks(r.getDimensionId()); Map<Long,Integer> order=ranks.stream().collect(Collectors.toMap(SszctopDimensionRank::getCareerId,SszctopDimensionRank::getRankOrder)); List<Long> standard=new ArrayList<>(cs); standard.sort(Comparator.comparing(order::get)); v.put("standardOrderIds",standard); v.put("rankDetails",ranks.stream().filter(x->cs.contains(x.getCareerId())).collect(Collectors.toList())); } return v; }
    private void notifyRoom(SszctopRoom r,String event){socketHandler.broadcast("sszctop/"+r.getGameId()+"/"+r.getRoomCode(),Collections.singletonMap("type",event));}
    private GkzhActivityGame game(Long id){return game(id,null);}
    /** 查询并校验职场TOP游戏属于指定活动实例。 */
    private GkzhActivityGame game(Long id,Long instanceId){GkzhActivityGame g=weekService.getGame(id);if(g==null||!"sszctop".equals(g.getGameType()))throw new IllegalArgumentException("职业认知游戏不存在");if(instanceId!=null&&!instanceId.equals(g.getInstanceId()))throw new IllegalArgumentException("该游戏不属于当前活动");return g;}
    private static Long id(Object o){return o==null?null:Long.valueOf(String.valueOf(o));} private static Integer integer(Object o){return o==null?null:Integer.valueOf(String.valueOf(o));}
    private static List<Long> ids(Object o){if(o==null)return new ArrayList<>();if(o instanceof Collection)return ((Collection<?>)o).stream().map(x->Long.valueOf(String.valueOf(x))).collect(Collectors.toList());return Arrays.stream(String.valueOf(o).split(",")).filter(x->!x.isEmpty()).map(Long::valueOf).collect(Collectors.toList());}
}
