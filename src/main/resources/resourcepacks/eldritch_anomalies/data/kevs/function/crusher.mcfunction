
execute at @s run particle minecraft:explosion_emitter ~ ~ ~ 1 1 1 0.2 5 force

execute at @s run playsound minecraft:block.anvil.land master @e[type=player,distance=..10] ~ ~ ~ 1 1

execute at @s run damage @p[distance=..5,limit=1,sort=nearest] 3