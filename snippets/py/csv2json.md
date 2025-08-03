# CSV to JSON Converter (Python)

## Description
Converts CSV formatted data into JSON format with automatic header detection and proper type handling.

## Input Requirements
- **Format**: CSV (Comma-Separated Values)
- **Structure**: 
  - First row must contain column headers
  - Subsequent rows contain data values
  - Standard CSV formatting rules apply
- **Encoding**: UTF-8 recommended
- **Line Endings**: Unix (LF) or Windows (CRLF) both supported

## Output Format
- **Type**: JSON array of objects
- **Structure**: Each row becomes a JSON object with headers as keys
- **Data Types**: All values are initially strings; numeric conversion may be needed post-processing
- **Format**: Pretty-printed with 2-space indentation

## Examples

### Example 1: Basic CSV conversion
**Input:**
```csv
name,age,city
John,25,New York
Jane,30,Los Angeles
Bob,22,Chicago
```
**Output:**
```json
[
  {
    "name": "John",
    "age": "25",
    "city": "New York"
  },
  {
    "name": "Jane",
    "age": "30",
    "city": "Los Angeles"
  },
  {
    "name": "Bob",
    "age": "22",
    "city": "Chicago"
  }
]
```

### Example 2: CSV with quoted fields
**Input:**
```csv
"Product Name","Price","Description"
"Laptop Computer","999.99","High-performance laptop"
"Wireless Mouse","29.99","Ergonomic design"
```
**Output:**
```json
[
  {
    "Product Name": "Laptop Computer",
    "Price": "999.99",
    "Description": "High-performance laptop"
  },
  {
    "Product Name": "Wireless Mouse",
    "Price": "29.99",
    "Description": "Ergonomic design"
  }
]
```

### Example 3: CSV with empty fields
**Input:**
```csv
id,name,email,phone
1,Alice,alice@example.com,
2,Bob,,123-456-7890
3,Charlie,charlie@example.com,987-654-3210
```
**Output:**
```json
[
  {
    "id": "1",
    "name": "Alice",
    "email": "alice@example.com",
    "phone": ""
  },
  {
    "id": "2",
    "name": "Bob",
    "email": "",
    "phone": "123-456-7890"
  },
  {
    "id": "3",
    "name": "Charlie",
    "email": "charlie@example.com",
    "phone": "987-654-3210"
  }
]
```

### Example 4: Single column CSV
**Input:**
```csv
numbers
1
2
3
4
5
```
**Output:**
```json
[
  {
    "numbers": "1"
  },
  {
    "numbers": "2"
  },
  {
    "numbers": "3"
  },
  {
    "numbers": "4"
  },
  {
    "numbers": "5"
  }
]
```

### Example 5: Empty CSV
**Input:**
```csv

```
**Output:**
```json
[]
```

## Error Handling
- Empty input returns empty array: `[]`
- Malformed CSV will raise appropriate error messages
- Missing headers will cause all rows to use positional indices
- Special characters in headers are preserved as-is

## Usage Notes
- All values are returned as strings; convert to appropriate types as needed
- Supports standard CSV features including quoted fields and escaped quotes
- Large files are processed efficiently using streaming readers
- Unicode characters in data are fully supported
- Header names should be unique for predictable results

## Post-Processing Tips
To convert numeric strings to actual numbers:
```python
import json

data = json.loads(csv_json_output)
for row in data:
    if 'age' in row:
        row['age'] = int(row['age'])
    if 'price' in row:
        row['price'] = float(row['price'])
```