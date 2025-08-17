from flask import Flask, request, jsonify, render_template
import os
import subprocess
import sys

app = Flask(__name__)

SNIPPETS_DIR = os.path.join(os.path.dirname(__file__), 'snippets')
PY_DIR = os.path.join(SNIPPETS_DIR, 'py')
LUA_DIR = os.path.join(SNIPPETS_DIR, 'lua')


def list_snippets():
    snippets = []
    
    # Python snippets
    if os.path.exists(PY_DIR):
        for filename in sorted(os.listdir(PY_DIR)):
            name, ext = os.path.splitext(filename)
            if ext.lower() == '.py':
                snippets.append({
                    'id': name,
                    'title': name,
                    'description': f'Python snippet: {name}',
                    'language': 'python',
                    'filename': filename
                })
    
    # Lua snippets
    if os.path.exists(LUA_DIR):
        for filename in sorted(os.listdir(LUA_DIR)):
            name, ext = os.path.splitext(filename)
            if ext.lower() == '.lua':
                snippets.append({
                    'id': name,
                    'title': name,
                    'description': f'Lua snippet: {name}',
                    'language': 'lua',
                    'filename': filename
                })
    
    return snippets

def run_python_snippet(snippet_path, user_input):
    try:
        # Ensure user_input is properly formatted
        if isinstance(user_input, str):
            input_str = user_input
        elif isinstance(user_input, bytes):
            input_str = user_input.decode('utf-8')
        else:
            input_str = str(user_input)
            
        result = subprocess.run(
            [sys.executable, "-I", "-u", snippet_path],
            input=input_str,
            capture_output=True,
            text=True,
            timeout=5,
            encoding='utf-8'
        )
        return {
            'output': result.stdout,
            'error': result.stderr if result.returncode != 0 else None,
            'success': result.returncode == 0
        }
    except subprocess.TimeoutExpired:
        return {
            'output': '',
            'error': 'Execution timed out after 5 seconds',
            'success': False
        }
    except Exception as e:
        return {
            'output': '',
            'error': str(e),
            'success': False
        }

def run_lua_snippet(snippet_path, user_input):
    try:
        # Ensure user_input is properly formatted
        if isinstance(user_input, str):
            input_str = user_input
        elif isinstance(user_input, bytes):
            input_str = user_input.decode('utf-8')
        else:
            input_str = str(user_input)

        import tempfile
        fd, temp_path = tempfile.mkstemp(suffix='.lua')
        try:
            with os.fdopen(fd, 'w', encoding='utf-8') as temp_file:
                temp_file.write(f'input_data = {repr(input_str)}\n')
                with open(snippet_path, 'r', encoding='utf-8') as f:
                    lua_code = f.read()
                temp_file.write(lua_code)

            result = subprocess.run(
                ['luajit', temp_path],
                capture_output=True,
                text=True,
                timeout=5,
                env=os.environ,
                encoding='utf-8'
            )
        finally:
            os.unlink(temp_path)

        return {
            'output': result.stdout,
            'error': result.stderr if result.returncode != 0 else None,
            'success': result.returncode == 0
        }
    except subprocess.TimeoutExpired:
        return {
            'output': '',
            'error': 'Execution timed out after 5 seconds',
            'success': False
        }
    except FileNotFoundError:
        return {
            'output': '',
            'error': 'luajit not found in system PATH',
            'success': False
        }
    except Exception as e:
        return {
            'output': '',
            'error': str(e),
            'success': False
        }

@app.route('/')
def index():
    return render_template('index.html')

@app.route('/list_snippets')
def api_list_snippets():
    return jsonify(list_snippets())

@app.route('/run', methods=['POST'])
def run_snippet():
    data = request.json
    snippet_id = data.get('snippet_id')
    user_input = data.get('user_input', '')
    language = data.get('language')
    
    if not snippet_id or not language:
        return jsonify({'error': 'Missing snippet_id or language'}), 400
    
    if language == 'python':
        snippet_path = os.path.join(PY_DIR, f'{snippet_id}.py')
        if not os.path.exists(snippet_path):
            return jsonify({'error': 'Python snippet not found'}), 404
        result = run_python_snippet(snippet_path, user_input)
    elif language == 'lua':
        snippet_path = os.path.join(LUA_DIR, f'{snippet_id}.lua')
        if not os.path.exists(snippet_path):
            return jsonify({'error': 'Lua snippet not found'}), 404
        result = run_lua_snippet(snippet_path, user_input)
    else:
        return jsonify({'error': 'Unsupported language'}), 400
    
    return jsonify(result)

@app.route('/docs/<language>/<snippet_id>')
def get_snippet_docs(language, snippet_id):
    """Get markdown documentation for a snippet"""
    if language == 'python':
        docs_path = os.path.join(PY_DIR, f'{snippet_id}.md')
    elif language == 'lua':
        docs_path = os.path.join(LUA_DIR, f'{snippet_id}.md')
    else:
        return jsonify({'error': 'Unsupported language'}), 400
    
    if not os.path.exists(docs_path):
        return jsonify({'error': 'Documentation not found'}), 404
    
    try:
        with open(docs_path, 'r', encoding='utf-8') as f:
            content = f.read()
        return jsonify({'content': content})
    except Exception as e:
        return jsonify({'error': str(e)}), 500

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=5000)