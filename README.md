![](https://files.worlio.com/users/bonkmaykr/http/git/embed/firestar.png)

**Firestar is a cross-platform Java-based mod manager for WipEout 2048**, which runs on a desktop/laptop computer and aims to allow easy installation of mods with very little hassle. It is compatible with both real PSVita hardware, as well as with emulators.

## Get Started: [>> CLICK HERE <<](https://git.worlio.com/bonkmaykr/firestar/wiki)

![](https://files.worlio.com/users/bonkmaykr/http/git/embed/2024-07-12_15-02.png)

# Installation
The latest version of Firestar will always be available from [Releases](https://git.worlio.com/bonkmaykr/firestar/releases).  
If you aren't sure which file to get:
- **Windows:** Download the latest installer [here](https://screwgravity.net/firestar/builds/stable/tetsuo/firestar_1.3_installer_winnt_x86-64.exe).
- **Linux:** Install one of the packages from the [Releases](https://git.worlio.com/bonkmaykr/firestar/releases) page:
  - Ubuntu, Mint, PopOS: `sudo dpkg -i firestar_ver_arch.deb` or `gdebi firestar_ver_arch.deb`
  - Fedora, Mageia, OpenSUSE, AlmaLinux: `sudo rpm -i firestar-ver-arch.rpm`
  - Arch, EndeavourOS, CachyOS, Steam Deck: `sudo pacman -U firestar-ver-arch.pkg.tar.zst`
- **All Platforms:** Grab the [JAR](https://screwgravity.net/firestar/builds/stable/tetsuo/firestar_1.3_portable.jar) and run it.

# System Requirements
- Windows 8 or newer, Mac OS X 10.14 or older, any modern version of Linux/BSD
  - If you have a Mac running 10.15 Catalina or newer, you should use Crossover to run the Windows version of Firestar instead.
- Java 17 or above.
- **Non-Windows systems only**: WINE
- **Version 1.3 and above only:** [Visual C++ 2012 libraries](http://www.microsoft.com/en-au/download/confirmation.aspx?id=30679)
- Do NOT install Firestar directly to your PSVita's storage. Some of it's file operations do not play nice with FAT filesystems.

# Building
You are always free to copy the Firestar source code and compile the latest work-in-progress version for yourself.  
Firestar uses the Gradle build system. We write our code and perform all our tests on Linux, but these same steps should apply to all major operating systems.

## With Intellij IDEA
1. Create a new project cloned from this Git repo
2. Open the run configurations editor by clicking "Default" in the top right corner and selecting "Edit Configurations".
3. Set the JDK to Java 17, the module to firestar.firestar.main and the main class to Firestar's Main class.
4. Go to Modify Options > Add before launch task, then add a Gradle "clean" task
5. Go there again and add a Gradle "build" task
6. Delete the default build task that has now appeared as step 1.
7. Save your options and click the Play button to compile and test.

## From the command line
1. Ensure you have both Git and Gradle installed on your system.
- Windows: Visit [git-scm.com](https://git-scm.com/downloads) and the [Gradle Installation Guide](https://gradle.org/install/).
- Linux/BSD: Install the `git` and `gradle` packages from your package manager. For example:
  - Arch-based: `sudo pacman -Sy git gradle` (if this breaks your computer then run -Syu to rectify it)
  - Debian-based: `sudo apt install git gradle`
- Mac: Figure it out! None of us on the dev team use Macs and we can't be assed to.
2. Open the terminal and clone the git repo. This will download the Firestar source code to a `firestar/` folder in your current working directory.

    ```
    git clone https://git.worlio.com/bonkmaykr/firestar.git
    ```

3. `cd` to `firestar/` and run `gradle build` to make a JAR or `gradle run` to test your code immediately.
- On Windows systems make sure you're running `gradlew.bat` and not the Bash script instead.

# FAQ
## Can I use this on a Vita without enso/HEN?
No. You need a modded Vita in order to use this. Hacking a Vita is super easy these days, but if you don't want to, you can always use Vita3K instead.

## Where do I download mods?
There's no #1 place for Firestar mods as of now, but we can give you a few sample mods to test out if you are interested in seeing the program in action:
- [XP Rebalance](https://files.worlio.com/users/bonkmaykr/patches/2048/XPhack2.fstar): Reduces the amount of grinding required to level up for players who can't access the online campaign. **Leaderboard safe version!**
- [Pepsi Mod](https://files.worlio.com/users/bonkmaykr/http/reddit/pepsi_agility.fstar): Gives the FEISAR Agility ship a Pepsi makeover.
- [Gold AG-SYS Speed](https://files.worlio.com/users/bonkmaykr/patches/2048/gold3.fstar): Better Than Chrome™
- [Continue?](https://files.worlio.com/users/bonkmaykr/patches/2048/continue.fstar): Allows you to exit the pause menu without taking your thumb off the throttle. By ThatOneBonk
- [HD Skin](https://files.worlio.com/users/bonkmaykr/patches/2048/hd_skin.fstar): Recolors the UI to look like WipEout HD. By ThatOneBonk
- [Custom Soundtrack](https://files.catbox.moe/pcvfxj.fstar): A custom playlist I put together. This music is copyrighted, don't play it on stream

## What do I need to make my own mods?
For making your own mods, we recommend grabbing one of the many leaked PS Vita SDKs off of Archive.org since it has many of the tools you will need. I recommend using Photoshop CS5 for textures, since Sony officially provides a texture export plugin for CS5 in their SDK. There is also an included at9tool which can help with replacing sounds and music.

Many things like HUDs, custom translations, and even some game variables are kept in XML format and can be edited in any normal text editor like Microsoft Notepad, GNU Nano, GEdit or KWrite. (Please play nice when you're online.)

As for models and custom tracks, we don't have a concrete way of modifying those just yet, but work is being done on that, so stay tuned.

## (Non-Windows) About WINE Compatibility
Firestar itself can run natively on the system, but it needs WINE in order to handle the game assets because many of the utilities it runs in the background are for Windows only. If you're curious, a list of these tools is available in the "[Third Party Code](#third-party-code)" section below.
**If you are on Mac OS X you will need an older OS version that is still compatible with 32-bit applications.**

# Developer Commentary
## Programmer SOP
When adding new features to Firestar, the Screw Gravity! team (loosely) follows this procedure:
1. The developer prototypes the feature, in part or in full, on a Linux system.
2. After completion, a QEMU/KVM running Windows 10 w/ VC++ 2012 libs and the latest JDK is deployed to test Firestar on.
3. Code refactoring is done to account for Windows bugs. 
4. The final commit is pushed.

Try to stick to these conventions:
- Avoid allowing the user to interact with the main window (MissPiggy.java) while another menu is open that alters any data, and keep this window Always-on-top. This is usually done by passing the master JFrame as a parameter to the child and telling it to freeze the parent's JFrame until the user closes that menu.
- Keep formatting consistent and readable. 
    - `if` statements should have a space separating the conditions and code block.
    - Use tabs instead of repeat spaces for indentation where possible and keep one indentation per layer, avoid flattening unless appropriate.
    - Split up long equations and statements into new lines with an operator at the start of the new line.
    - Divide really long functions into paragraph for certain major steps.

## Third Party Code
Firestar is built on top of the following additional software:
- *[WINE](https://www.winehq.org/), for running the below applications across platforms.
- *[ffmpeg](https://ffmpeg.org/), for audio conversion in the OST generator
- †[org.json](https://github.com/stleary/JSON-java), Java library for handling JSON data.
- †[zip4j](https://github.com/srikanth-lingala/zip4j), A Java library for ZIP files
- [pkg2zip](https://github.com/mmozeiko/pkg2zip) & [psvpfsparser](https://github.com/motoharu-gosuto/psvpfstools), for (legal) asset decryption from Playstation Network.
- [libcurl](https://curl.se/libcurl/), allows psvpfsparser to communicate with Henkaku's F00D server.
- psp2psarc, Sony's proprietary PSARC compression/decompression utility.
- at9tool, Sony's proprietary ATRAC9 codec.

*_WINE, ffmpeg, and the Java Runtime are pulled as package dependencies on Linux systems._  
†_Libraries baked directly into the executable._

All of these, save for psp2psarc, appear in Tetsuo (1.3) and onwards. The licenses for Sony's psp2psarc and at9tool are polar opposite from Firestar's own GPL, so you should not expect Firestar to be a completely "libre" application (but then again, you're a WipEout player, the free software movement clearly isn't a huge concern). The open source nature of Firestar is provided with the hopes of allowing others to critique, audit, and improve upon the software, not necessarily for idealogical reasons.

Ideally, in the future, all of this functionality can be built into Firestar natively without any fancy tricks.
## Why use a leaked Playstation SDK?
Because many third-party programs for opening PSARC files either have no ability to create new ones or are prone to corruption bugs. Using the one made by the developers of the file format is the best option as it's what the game developers would have used. Obviously this creates all kinds of legal grey areas, so I can't distribute Sony's proprietary software next to my code. It's downloaded separately during the setup process.

## Why Java?
Cross-platform support with no hassle, a built-in UI framework, and I don't need to sell my soul like every Electron or React 'Native' developer is doing these days.

## Why the name "Firestar"?
Firestar was a secret track in the original WipEout which you unlocked after beating the game at least once, the mod manager is just named after that. Major versions are named after pilots or tracks. Internal class names are named after Muppets characters as this is the same naming convention that Sony used to codename parts inside the PSVita's SOC.

## Fast Mode vs. Compatibility Mode
"Fast Mode" was a legacy option that was scrapped before it could be developed. It skipped the creation of a PSARC file and simply dumped the final game assets to their destination folder. This works great for emulators, and doesn't require proprietary stolen SDKs from SCE. However the filesystem on the Playstation Vita really hated this method and would glitch out when this was done, causing textures to be corrupted once placed on the memory card. The WipEout engine has no fault tolerance for malformed or missing assets, so it just crashes instantly when it encounters one. This is where Compatibility mode comes from, which is the default setting for 2048.