#!/bin/bash
trial=1
fixedThr=4
fixedFov=60
fov=15
trialTime=60000

echo "Grasp Experiment"
trial=1
while [ $trial -le 10 ]
do
	echo "Trial $trial" 
	(( trial++ ))

	thr=1
	while [ $thr -le 16 ]
	do
		"java" "-jar" "exp.jar" "$thr" "$fixedFov" "$trialTime" "RUG" "GRSP"
		"java" "-jar" "exp.jar" "$thr" "$fixedFov" "$trialTime" "RU" "GRSP"
		"java" "-jar" "exp.jar" "$thr" "$fixedFov" "$trialTime" "UNC" "GRSP"

		(( thr = thr*2 ))
	done


done


echo "FOV Experiment"
trial=1
while [ $trial -le 10 ]
do
	echo "Trial $trial" 
	(( trial++ ))

	fov=15
	while [ $fov -le 120 ]
	do
		"java" "-jar" "exp.jar" "$fixedThr" "$fov" "$trialTime" "RUG" "FOV"
		"java" "-jar" "exp.jar" "$fixedThr" "$fov" "$trialTime" "RU" "FOV"
		"java" "-jar" "exp.jar" "$fixedThr" "$fov" "$trialTime" "UNC" "FOV"

		(( fov = fov + 15 ))
	done
done

