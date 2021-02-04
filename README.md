# Java Protocol Library

The java protocol library provides a generic solution for collecting messages, debug information and warnings. 

## Overview

Larger applications often require a proper feedback back to the caller for complex operations. 

Let's assume a user wants to save a number of orders. The business logik will validate the data entered, calculate defaults depending on generic system settings, save it to a database and send out an email to the sales team.

Each part of this operation can lead to situations where the user, system administrator or other audiences need to be informed of what happened. 

* validation can lead to messages describing errors or inconsistencies but a message not always lead to an abortion of the operation itself. Each order can have its own set of validation messages and must be grouped accordingly.
* calculation of defaults is something that needs to be reported to a system administrator or to the configuration team. It can assume defaults which are correct from a business perspective and do not require the operation to be aborted either.
* saving data to the database in most cases will lead to a severe error if anything goes wrong. Yet, thevalidation results may have something to do with it and should be reported back.
* Lastly, sending an email may fail and depending on the business it is either a severe error or something that has to be reported back.

This example shows various parts of the business operation can lead to a number of messages and/or errors which must be distributed to the intended audience.
