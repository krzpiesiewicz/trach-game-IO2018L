#converting gimp native format to png
cd GimpGraphicsSources
xcf2png heart.xcf -o ../Assets/heart.png
xcf2png avatar.xcf -o ../Assets/avatar.png

#generating additional colored avatars based on "avatar.png"
cd ..
cd Assets
convert avatar.png xc:'rgb(250,127,0)' -fx 'u*v.p{0,0}' avatar1.png
convert avatar.png xc:'rgb(220,50,253)' -fx 'u*v.p{0,0}' avatar2.png
convert avatar.png xc:'rgb(0,124,233)' -fx 'u*v.p{0,0}' avatar3.png
convert avatar.png xc:'rgb(245,94,93)' -fx 'u*v.p{0,0}' avatar4.png
convert avatar.png xc:'rgb(20,225,33)' -fx 'u*v.p{0,0}' avatar5.png
