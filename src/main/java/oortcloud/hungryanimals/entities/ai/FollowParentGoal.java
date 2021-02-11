package oortcloud.hungryanimals.entities.ai;

import java.util.EnumSet;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.util.JSONUtils;
import oortcloud.hungryanimals.HungryAnimals;
import oortcloud.hungryanimals.entities.ai.handler.AIContainer;
import oortcloud.hungryanimals.entities.ai.handler.AIFactory;
import oortcloud.hungryanimals.entities.capability.ICapabilityAgeable;
import oortcloud.hungryanimals.entities.capability.ProviderAgeable;

public class FollowParentGoal extends Goal {
	protected MobEntity childAnimal;
	protected MobEntity parentAnimal;
	double moveSpeed;
	private int delayCounter;

	protected ICapabilityAgeable ageable;
	
	public FollowParentGoal(MobEntity animal, double speed) {
		this.childAnimal = animal;
		this.ageable = childAnimal.getCapability(ProviderAgeable.CAP).orElse(null);
		this.moveSpeed = speed;
		this.setMutexFlags(EnumSet.of(Flag.MOVE,Flag.JUMP));
	}

	/**
	 * Returns whether the Goal should begin execution.
	 */
	public boolean shouldExecute() {
		if (ageable == null) {
			return false;
		} else if (ageable.getAge() >= 0) {
			return false;
		} else {
			List<MobEntity> list = this.childAnimal.world.getEntitiesWithinAABB(this.childAnimal.getClass(),
					this.childAnimal.getBoundingBox().grow(8.0D, 4.0D, 8.0D), this::isParent);
			MobEntity entityanimal = null;
			double d0 = Double.MAX_VALUE;

			for (MobEntity entityanimal1 : list) {
				double d1 = this.childAnimal.getDistanceSq(entityanimal1);

				if (d1 <= d0) {
					d0 = d1;
					entityanimal = entityanimal1;
				}
			}

			if (entityanimal == null) {
				return false;
			} else if (d0 < 9.0D) {
				return false;
			} else {
				this.parentAnimal = entityanimal;
				return true;
			}
		}
	}

	protected boolean isParent(MobEntity parent) {
		ICapabilityAgeable age = parent.getCapability(ProviderAgeable.CAP).orElse(null);
		return age == null || age.getAge() >= 0;
	}

	/**
	 * Returns whether an in-progress Goal should continue executing
	 */
	public boolean shouldContinueExecuting() {
		if (ageable.getAge() >= 0) {
			return false;
		} else if (!this.parentAnimal.isAlive()) {
			return false;
		} else {
			double d0 = this.childAnimal.getDistanceSq(this.parentAnimal);
			return d0 >= 9.0D && d0 <= 256.0D;
		}
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void startExecuting() {
		this.delayCounter = 0;
	}

	/**
	 * Reset the task's internal state. Called when this task is interrupted by another one
	 */
	public void resetTask() {
		this.parentAnimal = null;
	}

	/**
	 * Keep ticking a continuous task that has already been started
	 */
	public void updateTask() {
		if (--this.delayCounter <= 0) {
			this.delayCounter = 10;
			this.childAnimal.getNavigator().tryMoveToEntityLiving(this.parentAnimal, this.moveSpeed);
		}
	}

	public static void parse(JsonElement jsonEle, AIContainer aiContainer) {
		if (!(jsonEle instanceof JsonObject)) {
			HungryAnimals.logger.error("AI Follow Parent must be an object.");
			throw new JsonSyntaxException(jsonEle.toString());
		}

		JsonObject jsonObject = (JsonObject) jsonEle;

		float speed = JSONUtils.getFloat(jsonObject, "speed");

		AIFactory factory = (entity) -> new FollowParentGoal(entity, speed);
		aiContainer.getTask().after(SwimGoal.class).before(WaterAvoidingRandomWalkingGoal.class).put(factory);
	}
}