from flask import Flask
from flask import request
import json
import boto3

import Checker

app = Flask(__name__)

client = boto3.Session(
    aws_access_key_id='AKIA6RTUQT2ZXZIL2HMX',
    aws_secret_access_key='a'
)

dynamodb = client.resource('dynamodb', region_name='us-east-2')
table = dynamodb.Table('CachedCorrections')


@app.route('/correct/best', methods=['POST'])
def correctBest():
    body = request.json

    item = table.get_item(
        Key={
            'Input': body["text"]
        }
    )
    if 'Item' in item:
        return json.dumps({'best': item['Item']['Corrected']}, ensure_ascii=False, indent=2).encode('utf8')

    words = list(Checker.correct_core(body["text"]))

    best_match = ''
    for word in words:
        if type(word) == list:
            best_match += word[0]
        else:
            best_match += word

    table.put_item(
        Item={
            'Input': body["text"],
            'Corrected': best_match
        }
    )

    return json.dumps({'best': best_match}, ensure_ascii=False, indent=2).encode('utf8')


@app.route('/correct/all', methods=['POST'])
def correctAll():
    body = request.json
    words = list(Checker.correct_core(body["text"]))

    return json.dumps({'all': words }, ensure_ascii=False, indent=2).encode('utf8')


if __name__ == '__main__':
    app.run(host='0.0.0.0', debug=True)
