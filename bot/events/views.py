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
from typing import Union 
SLACK_VERIFICATION_TOKEN = getattr(settings,'SLACK_VERIFICATION_TOKEN', None)
SLACK_BOT_USER_TOKEN = getattr(settings,'SLACK_BOT_USER_TOKEN', None)
Client = WebClient(SLACK_BOT_USER_TOKEN)


class Events(APIView):
    def handle_text(self, event_message):
        user = event_message.get('user')                      
        text = event_message.get('text').lower()                      
        channel = event_message.get('channel')
        def send_message( text):
            Client.chat_postMessage(
                    channel=channel,
                    text=text
            )
        def command_error(feature:str, corr_command: str):
            Client.chat_postMessage(
                    channel=channel,
                    blocks=[
                        {
                            "type": "section",
                            "text": {
                                    "type": "mrkdwn",
                                    "text": "Invalid usage of `{}` feature. ".format(feature)
                             }
                         },         
                        {
                            "type": "section",
                            "text": {
                                "type": "mrkdwn",
                                "text": "*HOW TO USE*"
                                }
                        },
                        {
                            "type": "section",
                            "text": {
                                "type": "mrkdwn",
                                "text": "{}".format(corr_command)
                                }
                        }
                        
                    ]
            )
        def calculate_message(run: Runs): 
            sub = Submissions.select(Submissions).join(Runs).where(Runs.id == run.id).first()
            Client.chat_postMessage(
                    channel=channel, 
                    blocks = [{
                            "type": "section",
                            "text": {
                                "type": "mrkdwn",
                                "text": "`Calculate` is used to calculate *additional* features (features not already calculated by Goldberry) on a submission/run."
                                }
                            },
                            {
                                "type": "section",
                                "fields": [
                                    {
                                        "type": "mrkdwn",
                                        "text": "*Run ID:*\n{}".format(run.id)
                                    },
                                    {
                                        "type": "mrkdwn",
                                        "text": "*Submission Hash:*\n{}".format(sub.lookup_hash)
                                    },
                                    {
                                        "type": "mrkdwn",
                                        "text": "*Submission Description:*\n{}".format(sub.description)
                                    },
                                    {
                                        "type": "mrkdwn",
                                        "text": "*Run Description:*\n{}".format(run.description)
                                    },
                                ]
                            },
                            {
                                "type": "section",
                                "text": {
                                    "type": "mrkdwn",
                                    "text": "Select the Feature you'd like to calculate for this run:"
                                    },
                                "accessory": {
                                    "type": "static_select",
                                    "placeholder": {
                                            "type": "plain_text",
                                            "text": "Select a feature..."
                                    },
                                    "options": [
                                        {
                                            "text": {
                                                "type": "plain_text",
                                                "text": "cd(10)"
                                            }, 
                                            "value": "calc:cd_10"
                                        },
                                        {
                                            "text": {
                                                "type": "plain_text",
                                                "text": "MI"
                                            },
                                            "value": "calc:mi"
                                        }
                                    ]
                                }
                            }
                    ]
            )
        if 'hi' in text:
            bot_text = 'Hi <@{}> :wave:'.format(user)
            send_message(bot_text)
        elif 'calculate' in text:
            split_words = text.split()
            calc_ind = split_words.index('calculate')
            if len(split_words[calc_ind:]) == 2:
                # Check if either a submission hash or a run ID
                # Under the assumption that submission hash and run_ID won't conlict. 
                calc_user_input = split_words[calc_ind+1]
                # Assume that run_ID won't reach over 12 digits
                if calc_user_input.isdigit() and len(calc_user_input) < 12:
                    calc_obj = Runs.select().where(Runs.id == calc_user_input).first()
                else: 
                    calc_obj = Runs.select(Runs).join(Submissions).where(Submissions.lookup_hash == calc_user_input).first()
                if calc_obj:
                    calculate_message(calc_obj)
                else:
                    send_message("The run associated with the submission hash/run ID you provided *{}* does not exist.".format(calc_user_input))
            else:
                command_error('calculate', '`calculate <INSERT_SUB_HASH_OR_RUN_ID_HERE>`')
        elif 'replace' in text:
            split_words = text.split()
            rep_ind = split_words.index('replace')
            if len(split_words[rep_ind:]) == 4 and (split_words[rep_ind + 2] in ['with', 'using']):
                old_hash = split_words[rep_ind + 1]
                new_hash = split_words[rep_ind + 3]
                old_sub = Submissions.select().where(Submissions.lookup_hash == old_hash).first()
                new_sub = Submissions.select().where(Submissions.lookup_hash == new_hash).first()
                if (old_sub is None):
                    send_message( "The old submission *{}* you have provided does not exist.".format(old_hash))
                elif (new_sub is None):
                    send_message( "The new submission *{}* you have provided does not exist.".format(new_hash))
                elif (new_sub == old_sub):
                    send_message( "The two submissions can't be the same.")
                else:
                    Client.chat_postMessage(
                                    channel=channel,
                                    blocks = [
                                        {
                                            "type": "section",
                                            "text": {
                                                "type": "mrkdwn",
                                                "text": "`Replace` is used to fix a submission with wrong layout/submission parameters. This method replaces the wrong parameters of a submission with the correct parameters found in another submission. This method uses an *in-place* fix. This means your `old_hash`'s parameters will be replaced by the parameters of the `new_hash`. " }
                                        },
                                        {
                                            "type": "section",
                                            "text": {
                                                "type": "mrkdwn",
                                                "text": "You have requested to make the following replacement."
                                                }
                                        },
                                        {
                                            "type": "section",
                                            "fields": [
                                                {
                                                    "type": "mrkdwn",
                                                    "text": "*Old Submission Hash:*\n{}".format(old_hash)
                                                },
                                                {
                                                    "type": "mrkdwn",
                                                    "text": "*New Submission Hash:*\n{}".format(new_hash)
                                                },
                                                {
                                                    "type": "mrkdwn",
                                                    "text": "*Old Submission Description*\n {}".format(old_sub.description)
                                                },
                                                {
                                                    "type": "mrkdwn",
                                                    "text": "*New Submission Description:*\n{}".format(new_sub.description)
                                                },
                                            ]
                                        },
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
                                    ],
                        )
            else:
                command_error('replace', "`replace <INSERT_OLD_HASH> using <INSERT_NEW_HASH_HERE>`")
    def post(self, request, *args, **kwargs):
        slack_message = request.data
        if slack_message.get('token') != SLACK_VERIFICATION_TOKEN:
            return Response(status=status.HTTP_403_FORBIDDEN)
        if slack_message.get('type') == 'url_verification':
            return Response(data=slack_message.get('challenge'),
                            status=status.HTTP_200_OK)
        if ('event' in slack_message):                             
            event_message = slack_message.get('event')
            # Bot should never respond to itself
            if event_message.get('subtype') == 'bot_message':    
                return Response(status=status.HTTP_200_OK)
            if event_message.get('text'):
                self.handle_text(event_message)
        return Response(status=status.HTTP_200_OK)
