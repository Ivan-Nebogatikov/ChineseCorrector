import json
import boto3

client = boto3.Session(
    aws_access_key_id='AKIA6RTUQT2ZXZIL2HMX',
    aws_secret_access_key='a'
)

dynamodb = client.resource('dynamodb', region_name='us-east-2')
table = dynamodb.Table('CachedCorrections')


def lambda_handler(event, context):
    body = json.loads(event['body'])
    print(body)
    if ('Id' in body) and ('Corrected' in body) and ('Input' in body) and ('IsLike' in body):
        item = table.get_item(
            Key={
                'Input': body['Input']
            }
        )
        if not 'Item' in item:
            return {
                'statusCode': 500,
                'body': 'value not found'
            }
        if not 'Liked' in item['Item']:
            response = table.update_item(
                Key={
                    'Input': body['Input']
                },
                UpdateExpression="set Liked = :l, Disliked=:d",
                ExpressionAttributeValues={
                    ':l': [],
                    ':d': []
                },
                ReturnValues="UPDATED_NEW"
            )
            item['Item']['Liked'] = []
            item['Item']['Disliked'] = []
            
        if (body['Id'] in item['Item']['Liked']) or (body['Id'] in item['Item']['Disliked']):
                return {
                'statusCode': 500,
                'body': 'you already voted'
            }
        columnName = "Disliked"
        if body['IsLike']:
            columnName = "Liked"
        response = table.update_item(Key={
                'Input': body['Input']
            },
            UpdateExpression="set #ri = list_append(#ri, :val)",
            ExpressionAttributeNames={
                "#ri" : columnName
            },
            ExpressionAttributeValues={
                ':val': [ body['Id'] ]
            },
            ReturnValues="UPDATED_NEW"
        )
        return {
                'statusCode': 200,
                'body': json.dumps(response, indent=2)
            }
    else:
         return {
                'statusCode': 400,
                'body' : 'please add id, isLiked, input and corrected values'
            }
