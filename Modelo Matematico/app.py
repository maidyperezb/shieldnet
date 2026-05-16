from flask import Flask, jsonify
from shieldnet_optimizacion import run_optimization

app = Flask(__name__)


@app.route('/optimize', methods=['GET'])
def optimize():
    result = run_optimization()
    return jsonify(result)


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=int(__import__('os').environ.get('PORT', 5060)), debug=False)
