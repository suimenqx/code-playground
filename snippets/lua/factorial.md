# Factorial Calculator (Lua)

## Description
Calculates the factorial of a given non-negative integer using recursive approach.

## Input Requirements
- **Format**: Single integer number
- **Range**: 0 to 20 (higher values may cause stack overflow or integer overflow)
- **Type**: Non-negative integer

## Output Format
- **Type**: Single integer
- **Format**: Plain number without any additional text
- **Example**: `120` (for input `5`)

## Examples

### Example 1: Basic factorial calculation
**Input:**
```
5
```
**Output:**
```
120
```

### Example 2: Factorial of 0
**Input:**
```
0
```
**Output:**
```
1
```

### Example 3: Factorial of 10
**Input:**
```
10
```
**Output:**
```
3628800
```

## Error Handling
- Non-numeric input defaults to 0
- Negative numbers will return 1 (base case)
- No explicit error messages are provided for invalid input

## Usage Notes
- This implementation uses recursion which has a limit around 20,000 calls in most Lua environments
- For very large numbers (>20), consider using an iterative approach or a big integer library