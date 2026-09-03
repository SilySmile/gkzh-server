-- 补齐心愿橱窗缺失的 PNG 展示图
-- 说明：EPS 不能直接被浏览器/小程序 <image> 渲染，已在服务器上用 Ghostscript
-- 将以下 EPS 转换为 PNG，并放在 /imgs/xycc/png/ 下。

UPDATE `xycc_pattern`
SET `img_url` = CASE `description`
  WHEN '报纸' THEN 'https://zhiye.sxgkzh.cn/imgs/xycc/png/baozhi.png'
  WHEN '黄金首饰' THEN 'https://zhiye.sxgkzh.cn/imgs/xycc/png/huangjinshoushi.png'
  WHEN '商务投影仪' THEN 'https://zhiye.sxgkzh.cn/imgs/xycc/png/shangwutouyingyi.png'
  WHEN '职业装' THEN 'https://zhiye.sxgkzh.cn/imgs/xycc/png/zhiyezhuang.png'
  WHEN '个人名片' THEN 'https://zhiye.sxgkzh.cn/imgs/xycc/png/gerenmingpian.png'
  WHEN '蓝牙音响' THEN 'https://zhiye.sxgkzh.cn/imgs/xycc/png/lanyayinxiang.png'
  ELSE `img_url`
END
WHERE `description` IN ('报纸', '黄金首饰', '商务投影仪', '职业装', '个人名片', '蓝牙音响');
