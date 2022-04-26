local key = KEYS[1]
local value = redis.call("incr", key)

local expire = ARGV[1]

if value == 1 then
    redis.call("expire", key, tonumber(expire))
end
return value
