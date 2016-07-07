![alt text](https://raw.githubusercontent.com/CreaRo/DAWebmail/master/Screenshots/envelope_l.png "DAWebmail")

# DAWebmail

An open source Android client for Zimbra Webmail. 

## Overview

DAWebmail is an application developed for Zimbra Webmail users. This is the first exclusive Zimbra Webmail client for Android - Zimbra doesn't have an application of its own.

DAWebmail is currently being tested and used extensively in [DA-IICT](https://webmail.daiict.ac.in/). The application has 1000 active users within the university. The application can be used by any organization that uses Zimbra as their email service, by changing a minimal number of Strings and URLs in the app configuration files.

- View & Read Webmails - Even when offline. All mails are stored locally and can be viewed even when not connected to a network
- Download webmail attachments
- Receive notifications - pull based mechanism, you can set data usage to lower the interval in which network calls are made to check for new webmails
- Send (without attachments), Delete, Multi Delete, Mark as read/unread
- **Multiple Accounts** - Switch between as many webmail accounts as you have
- A *smartbox* to view interesting statistics about your webmails.

## Preview

[DAWebmail on Google Play](https://play.google.com/store/apps/details?id=com.sigmobile.dawebmail)

![alt text](https://raw.githubusercontent.com/CreaRo/DAWebmail/master/Screenshots/v5_activities_1_s.png "Activities-1")
![alt text](https://raw.githubusercontent.com/CreaRo/DAWebmail/master/Screenshots/v5_activities_2_s.png "Activities-2")

## Contributing

### ToDos & How
- The [issues](https://github.com/CreaRo/DAWebmail/issues) page has a list of ToDos. Feel free to put in suggestions, post new feature requests on the issues page, with the tag `feature-request`.
- Fork the master branch. Please remember to create a new branch (`git checkout -b feature-'name-of-feature'`) before making any changes, and send PRs for your non-master branch. Feel free to write to me before sending a pull-request. :)

### Conventions
- The code does not follow Hungarian notation. Simple **camelCase, everywhere**.
- Declare all strings/dimensions/colors in R.string, R.dimens, and R.color. Please do **not** hard code strings.
- Maintain the package structure being followed.
- Use ButterKife(7.0.1) to make all view references.

### Code

##### Network APIs
**OkHTTP**
The network requests used are a mixture of both Zimbra's REST and Soap APIs. This shall later be converted entirely to SOAP based architecture as webmail has complete support for SOAP API.

##### Database 
**[SugarORM](https://github.com/satyan/sugar)** Webmails are stored locally and can be viewed when offline.

##### Navigation Drawer & UI
**[MaterialDrawer - Mike Penz](https://github.com/mikepenz/MaterialDrawer)** AccountHeaders are used to switch between multiple accounts.

## Organizations/Universities using DAWebmail
- [DA-IICT](http://daiict.ac.in/)

### List Your Organization

DAWebmail can be used by any organization that uses Zimbra Webmail as their email service. 

The following are the changes to be made to `res/values/strings.xml`

- `webmail_domain` - The domain that follows post your Zimbra webmail ID (eg., 201301431@**daiict.ac.in**)
- `webmail_domain_url` - The domain of your Webmail service (eg., **https://webmail.daiict.ac.in/**)

And that's it :) DAWebmail is ready to run in your university/organization. Please write to me to get your university listed here.

## Developed By
**Rishabh Bhardwaj**
- [bhardwaj.rish@gmail.com](bhardwaj.rish@gmail.com)
- [website](http://rish.pythonanywhere.com) - [blog](http://bhardwajrish.blogspot.in)

### Contributors

- [Anurag Agarwal](https://github.com/anuragagarwal561994) | Soap API
- [Pradeet Swamy](https://github.com/Pradeet) | Development contributions
- Ankit Lakra | Logo
