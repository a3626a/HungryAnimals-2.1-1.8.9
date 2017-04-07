package oortcloud.hungryanimals.configuration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.JsonSyntaxException;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import oortcloud.hungryanimals.HungryAnimals;
import oortcloud.hungryanimals.core.lib.References;
import oortcloud.hungryanimals.entities.properties.handler.HungryAnimalManager;

public class ConfigurationHandlerJSONLoader {
	
	private BiConsumer<File, Class<? extends EntityAnimal>> read;
	private File directory;
	private String descriptor;
	
	/**
	 * 
	 * @param event
	 * @param descriptor : relative path, never start with /
	 * @param read
	 */
	public ConfigurationHandlerJSONLoader(FMLPreInitializationEvent event, String descriptor, BiConsumer<File, Class<? extends EntityAnimal>> read) {
		this.descriptor = descriptor;
		this.directory = new File(event.getModConfigurationDirectory() + "/" + References.MODNAME + "/" + descriptor);
		this.read = read;
	}
	
	public void sync() {
		if (!directory.exists()) {
			try {
				Files.createDirectories(directory.toPath());
			} catch (IOException e) {
				HungryAnimals.logger.error("Couldn\'t create {} folder {}\n{}", new Object[] { descriptor, directory, e });
				return;
			}
		}

		for (Class<? extends EntityAnimal> i : HungryAnimalManager.getInstance().getRegisteredAnimal()) {
			File iFile = new File(directory, EntityList.CLASS_TO_NAME.get(i) + ".json");

			if (!iFile.exists()) {
				createDefaultConfigurationFile(iFile);
			} else {
				try {
					this.read.read(iFile, i);
				} catch (JsonSyntaxException e) {
					HungryAnimals.logger.warn("Couldn\'t load {} {} of {}\n{}", new Object[] { this.descriptor, iFile, i, e });
				}
			}
		}
	}
	
	private void createDefaultConfigurationFile(File file) {
		String resourceName = "/assets/" + References.MODID + "/" + this.descriptor + "/" + file.getName();
		URL url = ConfigurationHandlerJSONLoader.class.getResource(resourceName);

		if (url == null) {
			HungryAnimals.logger.warn("Couldn\'t load {} {} from assets", new Object[] { this.descriptor, resourceName });
			return;
		}

		String s;

		try {
			s = Resources.toString(url, Charsets.UTF_8);
			file.createNewFile();
			FileWriter o = new FileWriter(file);
			o.write(s);
			o.close();
		} catch (IOException e) {
			HungryAnimals.logger.warn("Couldn\'t load {} {} from {}\n{}", new Object[] { this.descriptor, file, url, e });
		}
	}
	
	@FunctionalInterface
	public static interface BiConsumer<T, U> {
		public void read(T file, U entity);
	}
	
}
