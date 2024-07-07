#!/usr/bin/env bash

export tenant="basictiertestoutlook.onmicrosoft.com"
export subscription="50328023-df85-46b6-96f5-c4566d7b063c"
export location="centralus"
export resource_name_prefix="rujche24070703"

export resource_group="${resource_name_prefix}rg"
export eventhubs_namespace="${resource_name_prefix}ehn"
export eventhub="${resource_name_prefix}eh"
export schema_registry="${resource_name_prefix}sr"