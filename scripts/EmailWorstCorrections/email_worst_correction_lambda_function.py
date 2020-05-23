import json
import boto3
from boto3.dynamodb.conditions import Attr
from time import strftime, gmtime

client = boto3.Session(
    aws_access_key_id='AKIA6RTUQT2ZXZIL2HMX',
    aws_secret_access_key='a'
)

dynamodb = client.resource('dynamodb', region_name='us-east-2')
table = dynamodb.Table('CachedCorrections')

ses = boto3.client('ses', region_name='eu-west-2', aws_access_key_id='AKIA6RTUQT2ZWQSENIN2',
                   aws_secret_access_key='a')

def lambda_handler(event, context):
    res = table.scan(
        FilterExpression=Attr('Disliked').size().gt(0)
    )
    r = list(map(lambda x: {'Input': x['Input'], 'Corrected': x['Corrected'], 'DislikedCount': len(x['Disliked'])},
                 res['Items']))
    r.sort(key=lambda x: x['DislikedCount'], reverse=True)
    resp = ses.send_email(
        Source='your.mail@gmail.com',
        Destination={
            'ToAddresses': [
                'your.mail@gmail.com',
            ]
        },
        Message={
            'Subject': {
                'Data': 'Weekly update of the worst corrections: ' + strftime("%Y-%m-%d", gmtime()),
                'Charset': 'utf-8'
            },
            'Body': {
                'Text': {
                    'Data': json.dumps(r, ensure_ascii=False, indent=2),
                    'Charset': 'UTF-8'
                }
            }
        }
    )
    return {
        'statusCode': 200,
        'body': json.dumps(response, indent=2)
    } 