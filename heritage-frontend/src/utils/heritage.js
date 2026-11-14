/**
 * 非遗类别配色（唐卡矿物色）
 * 项目弹窗与「视界」地图共用，保证同一类别在两处颜色一致
 */
const CATEGORY_COLORS = {
  传统舞蹈: '#b4713a',
  传统戏剧: '#a6372c',
  传统医药: '#4f7a5e',
  民俗: '#d9a441',
  传统美术: '#c08136',
  传统技艺: '#5f7f99',
  传统音乐: '#6b8f7a',
  民间文学: '#8e6b4a',
  传统歌舞: '#a86b4c',
  传统体育游艺: '#7a7f4f',
  传统知识: '#6d6a8c'
}

export function categoryColor(category) {
  return CATEGORY_COLORS[category] || '#7d6a55'
}

export const CATEGORY_LEGEND = Object.entries(CATEGORY_COLORS).map(([name, color]) => ({ name, color }))
