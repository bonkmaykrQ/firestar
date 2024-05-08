![](https://files.worlio.com/users/bonkmaykr/http/git/embed/firestar.png)

Firestar is a mod manager for WipEout 2048 which automatically handles sorting mods by priority and repacking game assets based on selected add-on packs. It runs on a desktop/laptop computer and aims to allow easy installation of mods for users who have only a surface level understanding of hacking the PSVita.  
  
# about 75% complete (config I/O in progress, basic systems functional)  
**What works:**
- Repacking mods
- Cross-platform support

**What doesn't work yet:**
- config loading
- managing mods within the program
- misc. tools (pack creator, soundtrack mod generator...)

![](https://files.worlio.com/users/bonkmaykr/http/git/embed/firestar_window.png)
  
# System Requirements
- You will need a computer running Microsoft Windows, or any POSIX-compliant desktop operating system such as Mac OS X, Linux, BSD, or MINIX (Firestar does not run directly on the PSVita). ***Wayland support has not been tested.***
- You will also need the Java Runtime Environment installed, at least version 17 or newer. Non-Windows systems will require WINE to be installed for some functionality.
- Please run Firestar on an EXT or NTFS partition as some of the larger file operations it performs may cause glitches in certain filesystems used on memory cards. Do NOT install Firestar directly to your PSVita's storage.

# Mod File Structure
Mod files are contained inside of ZIP archives with the metadata being stored inside of the archive's embedded comments. The "data" folder inside contains the actual game assets for repacking.  
Next to it at root level a pack.png will be supplied for displaying your mod's icon in later versions of Firestar. This feature is currently unimplemented.  
Later versions of Firestar will have a built-in tool to handle these for you, since some archive programs don't make it easy enough to do by hand.  
  
Typical mod metadata will look something like this:
```json
{
  "version": 1,
  "friendlyName": "Custom soundtrack",
  "loaderversion": 0,
  "author": "bonkmaykr",
  "description": "I need sugar"
}
```

# FAQ
## What is the difference between Compatibility Mode and Fast Mode?
Fast Mode is the most intuitive solution. It simply unpacks all of the assets into a folder for the game to read. This makes it trivial to go back and modify it later without repacking anything and it means you don't need stolen Sony-confidential equipment. However, PSVita and PSTV memory cards use the exFAT filesystem which really does not play nice with WipEout assets and causes all sorts of glitches, so using a genuine PSARC is an absolute necessity when playing on a real console. Compatibility mode uses a real PSARC tool to try and create mod files as similar to the original game files as possible and create minimal friction with the game engine. 

## (Non-Windows) Do I need WINE? And why?
Only if you use Compatibility Mode. psp2psarc.exe is a Windows-only tool so we call WINE internally when needed, but Firestar itself will run on any desktop platform with a compliant Java runtime.  
**If you are on Mac OS X you will need an older OS version that is still compatible with 32-bit applications.**

## Why do I need a leaked Playstation SDK?
Because many third-party programs for opening PSARC files either have no ability to create new ones or are prone to corruption bugs. Using the one made by the developers of the file format is the best option as it's what the game developers would have used. Obviously this creates all kinds of legal grey areas so I can't distribute Sony's proprietary software next to my code. It's downloaded separately.

## Why Java?
Cross-platform support with no hassle, a built-in UI framework, and I don't need to sell my soul like every Electron or React 'Native' developer is doing these days.  
  
## Why the name "Firestar"?
Firestar was a secret track in the original WipEout which you unlocked after beating the game at least once, the mod manager is just named after that. Major versions are named after pilots or tracks. Internal class names are named after Muppets characters as this is the same naming convention that Sony used to codename parts inside the PSVita's SOC.