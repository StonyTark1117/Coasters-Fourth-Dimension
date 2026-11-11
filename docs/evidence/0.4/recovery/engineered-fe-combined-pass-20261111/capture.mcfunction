fd validation recovery_fixture status Dev
execute store result score #FE fd402 run data get block 0 131 20 CoastersEngineeredEnergy
execute store result score #tick fd402 run time query gametime
tellraw @a [{"text":"FD402_FE tick="},{"score":{"name":"#tick","objective":"fd402"}},{"text":" energy="},{"score":{"name":"#FE","objective":"fd402"}}]
schedule function fd402:capture 20t replace
