-- Lua 脚本：批量去重操作
-- KEYS[1] 为 Redis key，ARGV[1] 为过期时间

local keys = KEYS
local expireTime = tonumber(ARGV[1])

local newKeys = {}
for _, key in ipairs(keys) do
    if redis.call('SISMEMBER', key, key) == 0 then
        redis.call('SADD', key, key)
        redis.call('EXPIRE', key, expireTime)
        table.insert(newKeys, key)
    end
end

return newKeys
