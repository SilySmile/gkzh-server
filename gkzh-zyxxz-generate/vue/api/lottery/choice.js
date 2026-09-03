import request from '@/utils/request'

// 查询职业信息站-学生选择记录列表
export function listChoice(query) {
  return request({
    url: '/lottery/choice/list',
    method: 'get',
    params: query
  })
}

// 查询职业信息站-学生选择记录详细
export function getChoice(choiceId) {
  return request({
    url: '/lottery/choice/' + choiceId,
    method: 'get'
  })
}

// 新增职业信息站-学生选择记录
export function addChoice(data) {
  return request({
    url: '/lottery/choice',
    method: 'post',
    data: data
  })
}

// 修改职业信息站-学生选择记录
export function updateChoice(data) {
  return request({
    url: '/lottery/choice',
    method: 'put',
    data: data
  })
}

// 删除职业信息站-学生选择记录
export function delChoice(choiceId) {
  return request({
    url: '/lottery/choice/' + choiceId,
    method: 'delete'
  })
}
