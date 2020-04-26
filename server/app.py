from flask import Flask
from flask import request
import json
import Checker

app = Flask(__name__)


@app.route('/correct', methods=['POST'])
def correct():
    z = request.json
    words = list(Checker.correct_core(z["text"]))

    best_match = ''
    for word in words:
        if type(word) == list:
            best_match += word[0]
        else:
            best_match += word

    return json.dumps({'best' : best_match, 'words': words}, ensure_ascii=False, indent=2).encode('utf8')


if __name__ == '__main__':
    app.run(host='0.0.0.0', debug=True)
