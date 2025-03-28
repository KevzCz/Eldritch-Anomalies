
execute at @s run particle minecraft:flame ~ ~1 ~ 1 1 1 0.2 25 force
execute at @s run playsound minecraft:entity.blaze.ambient master @e[type=player,distance=..10] ~ ~ ~ 1 1
execute at @s run effect give @s tipsylib:trail_blazing 30 2 true
execute at @s run effect give @s minecraft:fire_resistance 30 2 true