# Fibonacci Calculator (Python)

## Description
Calculates the nth Fibonacci number using an efficient iterative approach with O(n) time complexity and O(1) space complexity.

## Input Requirements
- **Format**: Single integer
- **Range**: 0 to 10^6 (practical limits may vary by system)
- **Type**: Non-negative integer
- **Constraints**: Must be a valid integer within Python's int range

## Output Format
- **Type**: Single integer
- **Format**: Plain number without any additional text
- **Precision**: Exact calculation for all valid inputs
- **Range**: Can handle very large Fibonacci numbers (Python's arbitrary precision integers)

## Examples

### Example 1: Base cases
**Input:**
```
0
```
**Output:**
```
0
```

**Input:**
```
1
```
**Output:**
```
1
```

### Example 2: Small Fibonacci numbers
**Input:**
```
5
```
**Output:**
```
5
```

**Input:**
```
10
```
**Output:**
```
55
```

### Example 3: Medium Fibonacci numbers
**Input:**
```
20
```
**Output:**
```
6765
```

**Input:**
```
30
```
**Output:**
```
832040
```

### Example 4: Large Fibonacci numbers
**Input:**
```
50
```
**Output:**
```
12586269025
```

**Input:**
```
100
```
**Output:**
```
354224848179261915075
```

### Example 5: Very large input
**Input:**
```
200
```
**Output:**
```
280571172992510140037611932413038677189525
```

## Error Handling
- **Invalid input format**: Returns error message "Error: Please provide a valid integer"
- **Negative numbers**: Treated as invalid input (error message returned)
- **Non-integer input**: Treated as invalid input (error message returned)
- **Empty input**: Treated as invalid input (error message returned)

## Performance Characteristics
- **Time Complexity**: O(n) - linear with input size
- **Space Complexity**: O(1) - constant space usage
- **Memory Usage**: Minimal - only stores two integers regardless of input size
- **Large Numbers**: Leverages Python's arbitrary precision integers

## Mathematical Definition
The Fibonacci sequence is defined as:
- F(0) = 0
- F(1) = 1
- F(n) = F(n-1) + F(n-2) for n > 1

## Usage Notes
- This implementation is optimized for both small and very large inputs
- For extremely large inputs (n > 10^6), consider using matrix exponentiation for O(log n) complexity
- The iterative approach prevents stack overflow issues present in recursive implementations
- Output values can be extremely large - consider memory constraints for n > 100,000

## Testing Examples
To test the implementation:
```bash
echo "7" | python fib.py  # Output: 13
echo "15" | python fib.py  # Output: 610
echo "25" | python fib.py  # Output: 75025
```