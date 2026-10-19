-- ============================================
-- 秒杀原子扣减脚本(在 Redis 服务端一次执行完,期间别的请求插不进来)
-- KEYS[1] = 库存 key      seckill:stock:{itemId}
-- KEYS[2] = 抢到标记 key   seckill:user:{itemId}
-- ARGV[1] = userId
-- 返回值契约(脚本与 Java 的通信协议):
--   1   成功(已扣减 + 已标记)
--   0   已售罄
--  -1   未预热(库存 key 不存在)
--  -2   已抢过(该 userId 已在标记 Set 里)
-- ============================================
if redis.call('exists', KEYS[1]) == 0 then
    return -1
end
-- 坑:get 返回字符串,直接比较是字典序("10" < "2" 成立),必须 tonumber
if tonumber(redis.call('get', KEYS[1]) or '0') <= 0 then
    return 0
end
if redis.call('sismember', KEYS[2], ARGV[1]) == 1 then
    return -2
end
redis.call('decr', KEYS[1])
redis.call('sadd', KEYS[2], ARGV[1])
return 1
