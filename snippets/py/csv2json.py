import sys
import csv
import json

def csv_to_json(csv_content):
    lines = csv_content.strip().split('\n')
    if not lines:
        return []
    
    reader = csv.DictReader(lines)
    return list(reader)

if __name__ == "__main__":
    try:
        csv_content = sys.stdin.read()
        if not csv_content.strip():
            print("[]")
            sys.exit(0)
        
        data = csv_to_json(csv_content)
        print(json.dumps(data, indent=2))
    except Exception as e:
        print(f"Error processing CSV: {e}", file=sys.stderr)
        sys.exit(1)