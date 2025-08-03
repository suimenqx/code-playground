# JSON to Lua Table Converter (Lua)

## Description
Converts JSON formatted strings into valid Lua table literals with proper formatting and syntax.

## Input Requirements
- **Format**: Valid JSON string
- **Types Supported**: Objects, arrays, strings, numbers, booleans, null
- **Structure**: Any valid JSON structure including nested objects and arrays

## Output Format
- **Type**: Lua table literal string
- **Format**: Properly formatted Lua syntax with indentation
- **Features**: 
  - String keys use dot notation when possible (e.g., `key = value`)
  - Complex keys use bracket notation (e.g., `["key-with-dash"] = value`)
  - Arrays maintain their order and structure

## Examples

### Example 1: Simple object
**Input:**
```json
{"name": "John", "age": 30, "active": true}
```
**Output:**
```lua
{
  name = "John",
  age = 30,
  active = true
}
```

### Example 2: Nested object with array
**Input:**
```json
{"users": [{"id": 1, "name": "Alice"}, {"id": 2, "name": "Bob"}], "total": 2}
```
**Output:**
```lua
{
  users = {
    {
      id = 1,
      name = "Alice"
    },
    {
      id = 2,
      name = "Bob"
    }
  },
  total = 2
}
```

### Example 3: Array of primitives
**Input:**
```json
["apple", "banana", "cherry"]
```
**Output:**
```lua
{
  "apple",
  "banana",
  "cherry"
}
```

### Example 4: Complex keys
**Input:**
```json
{"key-with-dash": "value", "123numeric": "value2"}
```
**Output:**
```lua
{
  ["key-with-dash"] = "value",
  ["123numeric"] = "value2"
}
```

### Example 5: Empty structures
**Input:**
```json
{"empty_array": [], "empty_object": {}}
```
**Output:**
```lua
{
  empty_array = {},
  empty_object = {}
}
```

## Error Handling
- Invalid JSON input returns empty table: `{}`
- Malformed JSON is handled gracefully with fallback to empty table
- Null values are converted to `nil`

## Usage Notes
- The converter includes a simple built-in JSON parser as fallback
- Output is formatted for readability with proper indentation
- Complex JSON structures with special characters in keys are handled correctly