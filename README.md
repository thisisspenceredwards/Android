### Fitness App  
#### Milestone 1  

Leonel DeAnda, Spencer Edwards, Kristina Leo  
Bitbucket Link: https://bitbucket.org/KristinaMLeo/androidproject/src/master/

Tasks Accomplished  
1) Create design mockups of application (Time Spent: 1 hour each) (Everyone)  

2) Create activities for main application features (landing page, exercise logs and graphs, finding activities nearby)  (Time Spent:  3 hours) (Kristina)   

    - UI for Activity Log is set up, but not complete. Need to improve. (1 hour) (Leonel + Spencer)
    - UI for Filling out Session. (1 hour) (Leonel)
        
3) Backend API Features: Step counter, biking, running, calorie expenditure.  

    - RecordingClient is set up, Start/End Activities are also set up. (1 hour) (Leonel)
    - Session Submission is set up (3 hours. Learning Google Fit API took a while) (Leonel)
    - Session Retrieval is set up (1 hour) (Leonel)
        
4) Graphs with MPAndroidChart. (In progress)

    - Implemented, but only as place holder for now. 2+ hours (Spencer)
        
5) Sign up and Log in with Google API. (Time Spent: 6+ hours+), (Completed)

    - Main issue revolved around different certificates, signing the certificate, and then configuring our old project to match new certificate. (Spencer)
        
6) Nearby locations with Google Maps Platform (in progress)  (Time Spent: 7 hours)  (Kristina)
7) Step Tracker: Dummy activity that will be removed by Milestone 2. Was used to experiment with Google Fit. (Spencer)


     Time spent learning new topics and debugging 10+  
     Deploying on Google Cloud
     Android Graph Api
     Creating a signed application


#### Milestone 2  

Tasks Accomplished  

1) User Finding Activities. (Completed) - Kristina 15 hours  
    
    - Allow user permissions for location services    
    - Implementation of Google Places API:  
        - Place Autocomplete: Allows user to search places with Google predictions based on location.  
        - Place Details: Returns details of a place selected.  
    - Geocoding API:  
        - Converting latitude and longitude to formatted addresses  
    - Google PlacesSearchResult:  
        - Place Data Fields: Retrieve data with the fields  
    - Implementation of Google Maps API:  
        - Display map to the user as a fragment  
        - Place markers on the map at locations retrieved by Google API  
        - Place buttons to toggle between indoor and outdoor activities  
     
2) Track Workout (5 hours) - Leonel and Spencer
    
    - TextView of the sessions was converted into a recycler view
    - Every session is clickable and launches an intent to view the session in a bit more detail
    - Automatic update of session view using Async Tasks when user inserts a new session
    - Added Distance, and CalorieCount to sessions and data recorded.
    - On insertSessionFailures, keep the end recording button as is so that the user can try again later.
    
3) ViewSession (5 hours) - Leonel
    - New Activity to display more info of the session clicked
    - Show Session Name, Description, Activity, Start and End time, Ellapsed time, and Calorie, Distance, and Step count.
    - Figuring out why Distance and Step Counter weren't registering. And if they were, some data was very off.
    
4) SessionInfo (1 hour) - Leonel
    - Added to the spinner a few more options for activity
    - Changed a bit of functionality in back end

5) Chart Fragment (30 hours) Spencer

    - Prints user's data to graph using Google's History API using AsyncTask
    - Bar data is time accurate (sessions are printed in the proper order)
    - Supports Calorie and Step counts
    - Supports Day, Week, Month, and Year time frames.
    - Graph updates when a user ends a session
    - Progress bar to indicate to the user the app is retrieving sessions
    - Labels Change to reflect changes in X and Y axes
    Editing Dialog: 
    
    - Allows a user to change the settings of the graph using two sets of radio buttons.  
        - Radio buttons remember settings
    
6) Testing on Actual Phone (3 hours) - Leonel
    - Using an actual phone to test distance and step tracking, and any updates to other features.
    
7) Presentation PPT (1-2 hours) - Kristina, Leonel, Spencer

8) Presentation and Demo (1 hour) - Kristina, Leonel


Sources Used:  
1) Google API
2) Google Maps Platform  
3) MPAndroidChart  
4) Google Fit  
5) Google Places API  
6) Google Geocoding  
7) Google PlacesSearchResult  