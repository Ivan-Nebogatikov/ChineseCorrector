import json
import boto3
from __future__ import print_function

client = boto3.Session(
    aws_access_key_id='AKIA6RTUQT2ZXZIL2HMX',
    aws_secret_access_key='a'
)

dynamodb = client.resource('dynamodb', region_name='us-east-2')
tableName = 'CachedCorrections'

def Create():
    table = dynamodb.create_table(
        TableName= tableName,
        KeySchema=[
            {
                'AttributeName': 'Input',
                'KeyType': 'HASH'  #Partition key
            }
        ],
        AttributeDefinitions=[
            {
                'AttributeName': 'Input',
                'AttributeType': 'S'
            }
        ],
        ProvisionedThroughput={
            'ReadCapacityUnits': 5,
            'WriteCapacityUnits': 5
        }
    )
    print("Table status:", table.table_status)

def Drop():
    table = dynamodb.Table(tableName)
    table.delete()