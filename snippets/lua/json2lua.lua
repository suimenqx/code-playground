-- Convert JSON string to Lua table literal

local json_str = input_data or "{}"

function json_to_lua_table(obj, indent)
    indent = indent or 0
    local spaces = string.rep("  ", indent)
    
    if type(obj) == "table" then
        local items = {}
        local is_array = #obj > 0
        
        for k, v in pairs(obj) do
            local key_str
            if is_array then
                key_str = ""
            else
                if type(k) == "string" and k:match("^[a-zA-Z_][a-zA-Z0-9_]*$") then
                    key_str = k .. " = "
                else
                    key_str = "[" .. string.format("%q", k) .. "] = "
                end
            end
            
            local value_str
            if type(v) == "table" then
                value_str = json_to_lua_table(v, indent + 1)
            elseif type(v) == "string" then
                value_str = string.format("%q", v)
            elseif type(v) == "number" or type(v) == "boolean" then
                value_str = tostring(v)
            else
                value_str = "nil"
            end
            
            table.insert(items, spaces .. "  " .. key_str .. value_str)
        end
        
        if #items == 0 then
            return "{}"
        else
            return "{\n" .. table.concat(items, ",\n") .. "\n" .. spaces .. "}"
        end
    else
        return string.format("%q", tostring(obj))
    end
end

-- Simple JSON parser (since we might not have json library)
function parse_json(str)
    str = str:gsub("%s+", ""):gsub("^%s*", ""):gsub("%s*$", "")
    
    if str == "" then return {} end
    
    -- Simple JSON parser
    local pos = 1
    
    local function skip_whitespace(s)
        while pos <= #s and s:sub(pos, pos):match("%s") do
            pos = pos + 1
        end
    end
    
    local function parse_value(s)
        skip_whitespace(s)
        
        if pos > #s then return nil, pos end
        
        local char = s:sub(pos, pos)
        
        if char == "{" then
            -- Object
            pos = pos + 1
            local obj = {}
            skip_whitespace(s)
            
            while pos <= #s and s:sub(pos, pos) ~= "}" do
                -- Parse key
                if s:sub(pos, pos) == '"' then
                    pos = pos + 1
                    local key_end = s:find('"', pos)
                    local key = s:sub(pos, key_end - 1)
                    pos = key_end + 1
                    
                    skip_whitespace(s)
                    if s:sub(pos, pos) == ":" then
                        pos = pos + 1
                        local value = parse_value(s)
                        obj[key] = value
                        skip_whitespace(s)
                        if s:sub(pos, pos) == "," then
                            pos = pos + 1
                            skip_whitespace(s)
                        end
                    end
                else
                    break
                end
            end
            
            if pos <= #s and s:sub(pos, pos) == "}" then
                pos = pos + 1
            end
            
            return obj
            
        elseif char == '"' then
            -- String
            pos = pos + 1
            local str_end = s:find('"', pos)
            local str_val = s:sub(pos, str_end - 1)
            pos = str_end + 1
            return str_val
            
        elseif char:match("[0-9-]") then
            -- Number
            local num_end = s:find("[^0-9.-]", pos)
            if not num_end then num_end = #s + 1 end
            local num_str = s:sub(pos, num_end - 1)
            pos = num_end
            return tonumber(num_str)
            
        elseif s:sub(pos, pos + 3) == "true" then
            pos = pos + 4
            return true
            
        elseif s:sub(pos, pos + 4) == "false" then
            pos = pos + 5
            return false
            
        elseif s:sub(pos, pos + 3) == "null" then
            pos = pos + 4
            return nil
            
        elseif char == "[" then
            -- Array
            pos = pos + 1
            local arr = {}
            skip_whitespace(s)
            
            while pos <= #s and s:sub(pos, pos) ~= "]" do
                local value = parse_value(s)
                table.insert(arr, value)
                skip_whitespace(s)
                if s:sub(pos, pos) == "," then
                    pos = pos + 1
                    skip_whitespace(s)
                end
            end
            
            if pos <= #s and s:sub(pos, pos) == "]" then
                pos = pos + 1
            end
            
            return arr
        end
        
        return nil
    end
    
    return parse_value(str)
end

local success, data = pcall(parse_json, json_str)
if success then
    print(json_to_lua_table(data))
else
    print("{}")
end
