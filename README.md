![](https://files.worlio.com/users/bonkmaykr/http/git/embed/firestar.png)

**Firestar is a cross-platform Java-based mod manager for WipEout 2048**, which runs on a desktop/laptop computer and aims to allow easy installation of mods for users with very little hassle. It is compatible with both real PSVita hardware, as well as emulators.  

## Get Started: [>> CLICK HERE <<](https://git.worlio.com/bonkmaykr/firestar/wiki)  

# Installation
The latest version of Firestar will always be available from [Releases](https://git.worlio.com/bonkmaykr/firestar/releases).  
If you aren't sure which file to get:
- **Windows:** Download the latest installer [here](https://files.worlio.com/users/bonkmaykr/tools/firestar/latest/firestar_1.2_installer_windows.exe).
- **Linux:** Install one of the packages from the [Releases](https://git.worlio.com/bonkmaykr/firestar/releases) page:
    - Ubuntu, Mint, PopOS: `sudo dpkg -i firestar_ver_arch.deb` or `gdebi firestar_ver_arch.deb`
    - Fedora, Mageia, OpenSUSE, AlmaLinux: `sudo rpm -i firestar-ver-arch.rpm`
    - Arch, EndeavourOS, CachyOS, Steam Deck: `sudo pacman -U firestar-ver-arch.pkg.tar.zst`  
- **All Platforms:** Grab the [Portable ZIP](https://files.worlio.com/users/bonkmaykr/tools/firestar/latest/firestar_dekka-1.2_portable.zip), extract it, and run the JAR file.
  
# System Requirements
- You will need a computer running Microsoft Windows, or any POSIX-compliant desktop operating system such as Mac OS X, Linux, BSD, or MINIX (Firestar does not run directly on the PSVita). ***Wayland support has not been tested.***
- You will also need the Java Runtime Environment installed, at least version 17 or newer. Non-Windows systems will require WINE to be installed for some functionality.
- Do NOT install Firestar directly to your PSVita's storage. Some of it's file operations do not play nice with FAT filesystems.

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
## Third Party Code
Firestar is built on top of the following additional software which is downloaded separately during runtime:
- [ffmpeg](https://ffmpeg.org/), for audio conversion in the OST generator
- [pkg2zip](https://github.com/mmozeiko/pkg2zip) & [psvpfsparser](https://github.com/motoharu-gosuto/psvpfstools), for (legal) asset decryption from Playstation Network.
- [FART](https://fart-it.sourceforge.net/), for find-and-replace patches. Yes, seriously, that's what it's called.
- [libcurl](https://curl.se/libcurl/), allows psvpfsparser to communicate with Henkaku's F00D server.
- psp2psarc, Sony's proprietary PSARC compression/decompression utility.
- at9tool, Sony's proprietary ATRAC9 codec.  
  
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