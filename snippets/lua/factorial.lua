-- Factorial calculation
local n = tonumber(input_data) or 0

function factorial(num)
    if num <= 1 then
        return 1
    else
        return num * factorial(num - 1)
    end
end

print(factorial(n))
