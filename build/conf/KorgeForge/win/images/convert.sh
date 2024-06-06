#convert -compress none headerlogo.png -compress none headerlogo.bmp
#convert -compress none logo.png -compress none logo.bmp
ffmpeg -y -i headerlogo.png headerlogo.bmp
ffmpeg -y -i logo.png logo.bmp
#convert idea_CE.png idea_CE.ico
#convert idea_CE_EAP.png idea_CE_EAP.ico
#convert install.png install.ico
#convert uninstall.png uninstall.ico
# convert -resize x16 -gravity center -crop 16x16+0+0 input.png -flatten -colors 256 -background transparent output/favicon.ico

