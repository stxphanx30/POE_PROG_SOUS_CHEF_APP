YouTube Link:  https://youtu.be/Y6fJGOLWKw0

## Story

SousChef is an android application designed to help make your Master Chef dreams a reality by providing you with  a plethora of recipes and also serves as a way for your to record your own recipes. The name says it all. SousChef makes reference to the role of a Sous Chef in a kitchen and that person is usually the second in command after the head chef, so basically the application is pointing out how you are the head chef and its your assistant.

## Features
### Authentication
- Login and Register details re stored locally using RoomDb

### Security
- to ensure that your secret recipes do not fall into the wrong hands we have put biometrics in place to prevent unauthorised access.
- The biometrics of the application are session based meaning that as long as the session is active and you have the biometrics setting toggled on, you will be prompted to use your fingerprint each time you open the application

### Settings
- users are able to toggle different settings as using the settings screen.

### API
- TheMealDb was the API used to fetch the bulk of the information on the applcation.

### Offline mode
- RoomDB was used to store data locally and make sure users can access their recipes withour requiring internet access

### Notification
- we used realtime notifications to remind users of meals they planned on making

## User guide
### Home screen
- random recipe slider to grab your attention and inspire you to try something new
- popular categories to choose form that take you to recipes that fall under the chosen category
- favourites section displays the recipes you have favourited for quick access

### My recipe
- displays recipes user has added

### Favourites screen
- displays recipes the user has favourited

### Pantry/Planner
- displays ingredients you have and ingredients you need to restock on in the pantry 
- allows user to meal plan with dates and reminders using notifications

### Settings
- allows the user to toggle the notifications  
- allows the user to select the provided languages

### minimun version supported 
- minimum version supported API 28 

### Declaration of AI Usage


The SousChef project made minimal use of AI in generating new information for the application. AI image generation was used only for creating the logo in Sora.ai, and Recraft.ai was used to convert that image into a vector format as well as language translation.

ChatGPT, GitHub Copilot, and Google Gemini were used during development. Copilot was primarily used to generate commit comments, while ChatGPT and Gemini assisted with troubleshooting errors and finding alternatives to deprecated libraries. ChatGPT was also occasionally used to help fix layout issues.

This summarizes the extent of AI usage in our project.
