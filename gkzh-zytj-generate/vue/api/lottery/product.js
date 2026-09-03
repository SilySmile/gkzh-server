import request from '@/utils/request'

// 查询职愿探究-MBTI商品列表
export function listProduct(query) {
  return request({
    url: '/lottery/product/list',
    method: 'get',
    params: query
  })
}

// 查询职愿探究-MBTI商品详细
export function getProduct(productId) {
  return request({
    url: '/lottery/product/' + productId,
    method: 'get'
  })
}

// 新增职愿探究-MBTI商品
export function addProduct(data) {
  return request({
    url: '/lottery/product',
    method: 'post',
    data: data
  })
}

// 修改职愿探究-MBTI商品
export function updateProduct(data) {
  return request({
    url: '/lottery/product',
    method: 'put',
    data: data
  })
}

// 删除职愿探究-MBTI商品
export function delProduct(productId) {
  return request({
    url: '/lottery/product/' + productId,
    method: 'delete'
  })
}
