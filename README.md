# Java Protocol Library

The java protocol library provides a generic solution for collecting messages, debug information and warnings. 

## Overview

Larger applications often require a proper feedback back to the caller for complex operations. 

Let's assume a user wants to save a number of orders. The business logik will validate the data entered, calculate defaults depending on generic system settings, save it to a database and send out an email to the sales team.

Each part of this operation can lead to situations where the user, system administrator or other audiences need to be informed of what happened. 

* validation can lead to messages describing errors or inconsistencies but a message not always lead to an abortion of the operation itself. Each order can have its own set of validation messages and must be grouped accordingly.
* calculation of defaults is something that needs to be reported to a system administrator or to the configuration team. It can assume defaults which are correct from a business perspective and do not require the operation to be aborted either.
* saving data to the database in most cases will lead to a severe error if anything goes wrong. Yet, the validation results may have something to do with it and should be reported back.
* Lastly, sending an email may fail and depending on the business it is either a severe error or something that has at least to be reported back.

This example shows that various parts of the business operation can lead to a number of messages and/or errors which must be distributed to the intended audience.

## Features

The protocol library provides the following features:

* Each message has a severity level. Similar to logging libraries, the protocol library provides the standard levels `debug`,`info`,`warn`,`error`. If required, custom levels can be implemented as a replacement or in addition to the standard levels.
* Messages can be tagged with labels. It provides a way to specify the target audience for the message. Eg. `user`, `sales-team`, `admin`.
* Messages can be grouped. It provides a way of structuring messages like files (= messages) in directories (= groups),
* Each message group can have a header message.
* Searching the protocol for messages by severity level and tags.
* Full customization for protocol formatting. There are several pre-defined, yet customizable, formatters. One of which is a html formatter with font awesome icons.
* Message formatting libraries can be easily integrated. The library provides a couple of default implementations. Eg.: `String.format(...)` and `java.text.MessageFormat`.

## Requirements

* at least Java 8
* [unbescape](https://mvnrepository.com/artifact/org.unbescape/unbescape) for the `HtmlProtocolFormatter` (optional)
* [message-format](https://mvnrepository.com/artifact/de.sayayi.lib/message-format) for the `MessageFormatFormatter` (optional)
