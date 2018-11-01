# StarboundModManager
Allows to manage mods from the game Starbound easily! (works with Steam)

The StarboundModManager enables the user to manage their mods from the game [Starbound](http://playstarbound.com/).  

This application is in no way associated with Starbound and their creators Chucklefish!  
It was only created by [DunkleMango](https://github.com/DunkleMango) for the purpose of simplifying the process of managing mods for the game.  

## Sections
[Setting up the application (v1.0)](#setting-up-the-application-v10)  
[Using the application correctly (v1.0)](#using-the-application-correctly-v10)  

## Setting up the application (v1.0)
Start by downloading the StarboundModManager.jar file or compile the code yourself.  
Next, run the jar file with Java.  

You should see a window that looks like this:  

![Application started](https://i.imgur.com/kCZEEzx.png)  

Now do not worry, if you don't see any files listed in the application.  
This just means that the default location where steam was expected to be, was not found or that you don't have any mods downloaded via steam yet.  

In case the steam directory was incorrect, you can select it by yourself.  
We want to select the Workshop directory, so please click on **[Choose input directory]**  

You will see, that a new windows has opened, please select the Steam-workshop directory with the path:  
**Steam\SteamApps\workshop\content\211820**  
The number **211820** corresponds to Starbound!  

![Select Steam-workshop path](https://i.imgur.com/dyMsU0I.png)  

Once we finished selecting this directory and the window closes, we continue by pressing the button  
**[Choose output directory]**  

A new window opens. In this case, we want to select the mods folder of Starbound with the path:  
**Steam\SteamApps\common\Starbound\mods**

![Select Starbound mods path](https://i.imgur.com/bE7YoNe.png)  

Once you finished, you are ready to use the StarboundModManager!  
Don't worry about having to repeat this process. All changes will be automatically saved in your home directory in a small config file! :)

## Using the application correctly (v1.0)
If you want to transfer mods from your Steam-workshop directory to your Starbound mods folder, you can follow this quick guide.  

Start by selecting all mods that you want to transfer from the left list of files (tick the checkboxes).  
Then press the button **[Transfer Selected]** and confirm the operation.  

This process will override any existing files with the same name in your mods directory (the list of mods on the right).  

![Application started](https://i.imgur.com/kCZEEzx.png)  

On the other hand, if you want to just delete a few mods from the mods directory of Starbound, you will have to select the mods on the right.  
Then press the button **[Delete selected]** check **carfully**, if those are the right files to delete and confirm the operation.  

This will get rid of any selected mods, **PERMANENTLY** removing them from the system.  
So the deleted files will not be moved to your recycle bin, but removed completely.  
