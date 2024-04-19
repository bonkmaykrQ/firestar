![](https://files.worlio.com/users/bonkmaykr/http/git/embed/firestar.png)

# about 30% complete (basic frontend in progress, nonfunctional)

Firestar is a mod manager for WipEout 2048 which automatically handles sorting mods by priority and repacking game assets based on selected add-on packs. It runs on a desktop/laptop computer and aims to allow easy installation of mods for users who have only a surface level understanding of hacking the PSVita.  
TODO: modlist window screenshot
  
# FAQ
## What is the difference between Compatibility Mode and Fast Mode?
Fast Mode is the most intuitive solution. It simply unpacks all of the assets into a folder for the game to read. This makes it trivial to go back and modify it later without repacking anything and it means you don't need stolen Sony-confidential equipment. However, PSVita and PSTV memory cards use the exFAT filesystem which really does not play nice with WipEout assets and causes all sorts of glitches, so using a genuine PSARC is an absolute necessity when playing on a real console. Compatibility mode uses a real PSARC tool to try and create mod files as similar to the original game files as possible and create minimal friction with the game engine. 

## (Linux Users) Do I need WINE?
Only if you use Compatibility Mode. psp2psarc.exe is a Windows-only tool so we call WINE internally when needed, but Firestar itself will run on any desktop platform with a compliant Java runtime.

## Why do I need a leaked Playstation SDK?
Because many third-party programs for opening PSARC files either have no ability to create new ones or are prone to corruption bugs. Using the one made by the developers of the file format is the best option as it's what the game developers would have used. Obviously this creates all kinds of legal grey areas so I can't distribute Sony's proprietary software next to my code. It's downloaded separately.

## Why Java?
Cross-platform support with no hassle, a built-in UI framework, and I don't need to sell my soul like every Electron or React 'Native' developer is doing these days.  
  
## Why the name "Firestar"?
Firestar was a secret track in the original WipEout which you unlocked after beating the game at least once, the mod manager is just named after that. Major versions are named after pilots or tracks. Internal class names are named after Muppets characters as this is the same naming convention that Sony used to codename parts inside the PSVita's SOC.