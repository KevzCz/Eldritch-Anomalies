
execute at @s run particle minecraft:angry_villager ~ ~1 ~ 1 1 1 0.2 10 force
execute at @s run playsound minecraft:entity.zombified_piglin.angry master @e[type=player,distance=..10] ~ ~ ~ 1 1
execute at @s run attribute @s additionalentityattributes:generic.hitbox_scale base set 1.5
execute at @s run attribute @s additionalentityattributes:generic.model_scale base set 1.5
execute at @s run effect give @s minecraft:strength 30 2 true
execute at @s run effect give @s minecraft:slowness 30 2 true