# JustInTimeAdaptiveIntervention
JITAI using off-the-shelf wearable devices for improving physical activity among individuals with spinal cord injury 
using manual wheelchair

We have developed a smartphone application (app) called the Personal Health Informatics and Rehabilitation Engineering (PHIRE) app 
for Android-based smartphones. The PHIRE app runs on an Android-based smartphone (e.g. Nexus 5 or 5X, LG Corp., Englewood Cliffs, NJ, USA),
and collects sensor data from a wrist-worn smartwatch (e.g. LG132 Urbane, LG Corp., Englewood Cliffs, NJ, USA), and a Bluetooth-based whee
l rotation monitor (e.g. PanoBike, Topeak Inc., Taichung, Taiwan) to detect wheelchair-based physical activities (PAs) in individuals with
spinal cord injury who use manual wheelchairs for mobility purposes. The smartwatch and wheel rotation monitor stream data to the 
smartphone. The PHIRE app uses a C4.5 decision tree [1] machine-learning algorithm to classify wheelchair-based PAs and estimate PA 
levels once per minute [2]. Wheelchair-based PAs include ‘resting,’ ‘arm-ergometry,’ ‘household activities,’ ‘activities that may involve 
some wheelchair movement,’ ‘wheelchair propulsion,’ ‘caretaker pushing,’ and ‘wheelchair basketball.’ The energy expenditure is estimated 
in a three-step process: 1) wheelchair-based PAs are detected in near-real-time, 2) the metabolic equivalent of a task (MET) for the 
wheelchair-based PA in individuals with SCI (paraplegia and tetraplegia) is obtained from a compendium listing activity-metabolic estimates
[3] , and 3) the energy expenditure is then estimated based on the METs and the weight of the individual using the PHIRE app [3]. The 
distance traveled in miles is calculated based on the wheelchair-wheel diameter and sensor reading from the wheel rotation monitor. Via 
the smartphone, the PHIRE app also collects ecological momentary assessments about the type of PAs an individual is performing during the 
day. These ecological momentary assessments responses allow researchers to validate whether the PA an individual self-reports to be 
performing is detected by the PHIRE app. All sensor data and logs are encrypted, saved on the smartphone, and uploaded to Google’s Firebase
Cloud Storage on an hourly basis. Data are then downloaded to a desktop computer, decrypted, and viewed using custom desktop data 
visualization software. 

The PHIRE app can provide feedback about energy expenditure and distance traveled every minute during the day, and overall energy 
expenditure and distance traveled for the day and the week. Individuals can view their feedback whenever they want. The PHIRE app can also
provide just-in-time adaptive intervention (JITAI) which includes providing proactively-prompted, real-time feedback through the smartphone
(audio and/or vibration: based on individuals’ choice) and smartwatch (vibration) when the individual performs a bout of moderate-intensity
(or higher) PA. The default setting of the app is a minimum of three continuous minutes of PA before providing personalized feedback. 
Personalization is based on the individual’s prior patterns of conducting bouts of moderate-intensity PA, as measured by the system. 
Individuals are also provided with congratulatory messages when they perform a moderate-intensity PA bout of at least three minutes. 
Following this, the congratulatory messages are provided every minute until the individual stops performing the moderate-intensity PA. 
Individuals also receive a congratulatory message when they reach and exceed their daily goal. The daily goal is personalized each 
subsequent day based on the individual’s pattern of performing moderate-intensity PA the day before. The congratulatory message contains
minutes of moderate-intensity PA performed and minutes remaining to attain their goal.

References
	
1.	Quinlan JR. C4.5: programs for machine learning: Morgan Kaufmann Publishers Inc.; 1993. 302 p.
2.	Hiremath SV, Intille SS, Kelleher A, Cooper RA, Ding D. Estimation of energy expenditure for wheelchair users using a physical 
    activity monitoring system. Archives of Physical Medicine and Rehabilitation. 2016;97(7):1146-53. e1.
3.	Collins EG, Gater D, Kiratli J, Butler J, Hanson K, Langbein WE. Energy cost of physical activities in persons with spinal cord 
    injury. Medicine and Science in Sports and Exercise. 2010;42(4):691-700.


