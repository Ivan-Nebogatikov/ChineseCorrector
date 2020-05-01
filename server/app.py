from flask import Flask
from flask import request
import json
import boto3
#import Checker

app = Flask(__name__)

client = boto3.Session(
        aws_access_key_id='AKIA6RTUQT2ZXZIL2HMX',
        aws_secret_access_key='a'
    )

dynamodb = client.resource('dynamodb', region_name='us-east-2')
table = dynamodb.Table('CachedCorrections')

@app.route('/correct', methods=['POST'])
def correct():

    z = request.json
    words = ['a']#list(Checker.correct_core(z["text"]))

    best_match = ''
    for word in words:
        if type(word) == list:
            best_match += word[0]
        else:
            best_match += word

    return json.dumps({'best' : best_match, 'words': words}, ensure_ascii=False, indent=2).encode('utf8')


if __name__ == '__main__':

    table.put_item(
        Item={
            'Input': '我不知都',
            'Corrected': "我不知道"
        }
    )
    app.run(host='0.0.0.0', debug=True)
