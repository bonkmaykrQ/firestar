![](https://files.worlio.com/users/bonkmaykr/http/git/embed/firestar.png)

Firestar is a mod manager for WipEout 2048 which automatically handles sorting mods by priority and repacking game assets based on selected add-on packs. It runs on a desktop/laptop computer and aims to allow easy installation of mods for users who have only a surface level understanding of hacking the PSVita.  

## Video Tutorial: [>> CLICK HERE <<](https://www.youtube.com/watch?v=Bi9fpqR2Ulw)
![](https://files.worlio.com/users/bonkmaykr/http/git/embed/firestar_window.png)
  
# System Requirements
- You will need a computer running Microsoft Windows, or any POSIX-compliant desktop operating system such as Mac OS X, Linux, BSD, or MINIX (Firestar does not run directly on the PSVita). ***Wayland support has not been tested.***
- You will also need the Java Runtime Environment installed, at least version 17 or newer. Non-Windows systems will require WINE to be installed for some functionality.
- Please run Firestar on an EXT or NTFS partition as some of the larger file operations it performs may cause glitches in certain filesystems used on memory cards. Do NOT install Firestar directly to your PSVita's storage.

# FAQ
## (Non-Windows) Do I need WINE? And why?
Yes. psp2psarc.exe is a Windows-only tool (also like super proprietary... sorry). Firestar can run natively on the system but it needs WINE in order to pack the game assets.
**If you are on Mac OS X you will need an older OS version that is still compatible with 32-bit applications.**

## Why do I need a leaked Playstation SDK?
Because many third-party programs for opening PSARC files either have no ability to create new ones or are prone to corruption bugs. Using the one made by the developers of the file format is the best option as it's what the game developers would have used. Obviously this creates all kinds of legal grey areas so I can't distribute Sony's proprietary software next to my code. It's downloaded separately.

## Why Java?
Cross-platform support with no hassle, a built-in UI framework, and I don't need to sell my soul like every Electron or React 'Native' developer is doing these days.  
  
## Why the name "Firestar"?
Firestar was a secret track in the original WipEout which you unlocked after beating the game at least once, the mod manager is just named after that. Major versions are named after pilots or tracks. Internal class names are named after Muppets characters as this is the same naming convention that Sony used to codename parts inside the PSVita's SOC.

## Fast Mode vs. Compatibility Mode
"Fast Mode" was a legacy option that was scrapped before it could be developed. It skipped the creation of a PSARC file and simply dumped the final game assets to their destination folder. This works great for emulators, and doesn't require proprietary stolen SDKs from SCE. However the filesystem on the Playstation Vita really hated this method and would glitch out when this was done, causing textures to be corrupted once placed on the memory card. The WipEout engine has no fault tolerance for malformed or missing assets, so it just crashes instantly when it encounters one. This is where Compatibility mode comes from, which is the default setting for 2048.