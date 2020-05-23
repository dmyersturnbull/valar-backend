from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from django.conf import settings
from slack import WebClient
from valarpy.Valar import Valar
from valarpy.global_connection import db as global_db
valar_obj = Valar()
valar_obj.open()
from valarpy.model import *
import json 
SLACK_VERIFICATION_TOKEN = getattr(settings,'SLACK_VERIFICATION_TOKEN', None)
SLACK_BOT_USER_TOKEN = getattr(settings,'SLACK_BOT_USER_TOKEN', None)
Client = WebClient(SLACK_BOT_USER_TOKEN)

def add_confirm_cancel_buttons():
        {
            "type": "actions",
            "elements": [
                {
                    "type": "button",
                    "text": {
                        "type": "plain_text",
                        "emoji": True,
                        "text": "Confirm"
                    },
                    "style": "primary",
                    "value": "confirm_req_replace"
                },
                {
                    "type": "button",
                    "text": {
                        "type": "plain_text",
                        "emoji": True,
                        "text": "Cancel"
                    },
                    "style": "danger",
                    "value": "cancel_req_replace"
                }
            ]
        }

def extract_values(obj, key):
    """Pull all values of specified key from nested JSON blocks payload."""
    arr = []

    def extract(obj, arr, key):
        """Recursively search for values of key in JSON tree."""
        if isinstance(obj, dict):
            for k, v in obj.items():
                if isinstance(v, (dict, list)):
                    extract(v, arr, key)
                elif isinstance(v, str) and key in v:
                    arr.append(v)
        elif isinstance(obj, list):
            for item in obj:
                extract(item, arr, key)
        return arr

    results = extract(obj, arr, key)
    return results

def replace(prev_hash, new_hash):
    #Get subs that correspond to submission_hashes
    prev_sub = Submissions.select().where(Submissions.lookup_hash == prev_hash).first()
    new_sub = Submissions.select().where(Submissions.lookup_hash == new_hash).first()
    garbage_hash = 'see:' + new_sub.lookup_hash[4:]
    prev_sub.lookup_hash = garbage_hash
    new_sub.lookup_hash = prev_hash
    prev_sub.save()
    new_sub.save()
    run = Runs.select().where(Runs.submission_id == prev_sub.id).first()
    if run is not None:
        # Experiment ID set to shadow relam. Run must be processed again.
        run.experiment_id = 1410
        run.save()

def calculate(run_id: int, feat_val: str):
    run = Runs.fetch(run_id)
    plate_type = Runs.select(PlateTypes).join(Plates).join(PlateTypes).where(Runs.id == run_id).first().plate.plate_type
    n_exp_feats = plate_type.n_rows * plate_type.n_columns
    n_act_feats = WellFeatures.select(WellFeatures.well_id, Wells.id, Wells.run_id).join(Wells).where((WellFeatures.type == Features.fetch(feat_val)) & (Wells.run == run)).count()
    if (n_exp_feats == n_act_feats):
        print('yas')

class InteractiveComp(APIView):
    def post(self, request, *args, **kwargs):
        slack_data = request.data.get('payload')
        json_data = json.loads(slack_data)
        channel = json_data['channel']['id']
        response_url = json_data['response_url']
        ts = json_data['message']['ts']
        action = json_data['actions'][0]
        blocks = json_data['message']['blocks']
        # Check type of action and react based on type
        if action['type'] == 'button':
            action_val = action['value']
        elif action['type'] == 'static_select':
            sel_val_arr = action['selected_option']['value'].split(':')
            command_type = sel_val_arr[0]
            sel_val = sel_val_arr[1]
            if command_type == 'calc':
                selected_feat_block = [
                        {
                            "type": "section",
                            "fields": [
                                    {
                                        "type": "mrkdwn",
                                        "text": "*Selected Feature:*\n{}".format(sel_val)
                                    }
                                ]
                        }
                        ]
                # Replace last block with selected feature and add confirm/cancel buttons
                Client.chat_update(
                        channel=channel,
                        ts=ts,
                        blocks=blocks[:-1] + selected_feat_block
                )
            return Response(status=status.HTTP_200_OK)
        cancel_block = [
                {
                    "type": "divider"  
                },
                {
                "type": "section",
                "text": {
                        "type": "mrkdwn",
                        "text": "Request has been cancelled."
                    }
            }]
        confirm_block = [
                {
                    "type": "divider"
                },
                {
                    "type": "section",
                    "text": {
                        "type": "mrkdwn",
                        "text": "Request Confirmed. "
                        }
                }
            ]
        if  'confirm_req' in action_val:
            if 'replace' in action_val:
                old_hash = extract_values(json_data, 'Old Submission Hash')[0].split('\n')[1]
                new_hash = extract_values(json_data, 'New Submission Hash')[0].split('\n')[1]
                replace(old_hash, new_hash)
                confirm_block[1]['text']['text'] += 'The submission parameters of *{0}* have been replaced with the submission parameters of *{1}*.\n\n *{0}* is ready to use and good to go! '.format(old_hash, new_hash)
                Client.chat_update(
                    channel=channel,
                    ts=ts,
                    blocks= blocks[:-1] + confirm_block
                )
            elif 'calc' in action_val:
                run_id = extract_values(json_data, 'Run ID')[0].split('\n')[1]
                selected_feat = extract_values(json_data, 'feat_type:')[0].split(':')[1]
                #calculate(run_id)
        elif 'cancel_req' in action_val:
            Client.chat_update(
                    channel=channel,
                    ts=ts,
                    blocks=blocks[:-1] + cancel_block
            )
        return Response(status=status.HTTP_200_OK)
