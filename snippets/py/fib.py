import sys

def fibonacci(n):
    if n <= 1:
        return n
    a, b = 0, 1
    for _ in range(2, n + 1):
        a, b = b, a + b
    return b

if __name__ == "__main__":
    try:
        n = int(sys.stdin.read().strip())
        print(fibonacci(n))
    except ValueError:
        print("Error: Please provide a valid integer", file=sys.stderr)
        sys.exit(1)