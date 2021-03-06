package de.fhg.iais.roberta.factory.mbed.microbit;

import java.util.ArrayList;
import java.util.Properties;

import de.fhg.iais.roberta.components.Configuration;
import de.fhg.iais.roberta.factory.AbstractRobotFactory;
import de.fhg.iais.roberta.factory.ICompilerWorkflow;
import de.fhg.iais.roberta.factory.mbed.SimCompilerWorkflow;
import de.fhg.iais.roberta.inter.mode.action.ILightSensorActionMode;
import de.fhg.iais.roberta.inter.mode.action.IShowPicture;
import de.fhg.iais.roberta.syntax.Phrase;
import de.fhg.iais.roberta.syntax.check.program.RobotBrickCheckVisitor;
import de.fhg.iais.roberta.syntax.check.program.RobotSimulationCheckVisitor;
import de.fhg.iais.roberta.syntax.check.program.mbed.microbit.SimulationCheckVisitor;
import de.fhg.iais.roberta.util.RobertaProperties;
import de.fhg.iais.roberta.util.Util1;

public class Factory extends AbstractRobotFactory {

    private final CompilerWorkflow compilerWorkflow;
    private final SimCompilerWorkflow microbitSimCompilerWorkflow;
    private final Properties microbitProperties;
    private final String name;
    private final int robotPropertyNumber;

    public Factory() {
        this.microbitProperties = Util1.loadProperties("classpath:Microbit.properties");
        this.name = this.microbitProperties.getProperty("robot.name");
        this.robotPropertyNumber = RobertaProperties.getRobotNumberFromProperty(this.name);
        this.compilerWorkflow =
            new CompilerWorkflow(
                RobertaProperties.getStringProperty("robot.plugin." + this.robotPropertyNumber + ".compiler.resources.dir"),
                RobertaProperties.getStringProperty("robot.plugin." + this.robotPropertyNumber + ".compiler.dir"));
        this.microbitSimCompilerWorkflow = new SimCompilerWorkflow();
        Properties mbedProperties = Util1.loadProperties("classpath:Mbed.properties");
        addBlockTypesFromProperties("Mbed.properties", mbedProperties);
        addBlockTypesFromProperties("Microbit.properties", this.microbitProperties);
    }

    @Override
    public IShowPicture getShowPicture(String picture) {
        return null;
    }

    @Override
    public ILightSensorActionMode getLightActionColor(String mode) {
        return null;
    }

    @Override
    public String getFileExtension() {
        return "py";
    }

    @Override
    public ICompilerWorkflow getRobotCompilerWorkflow() {
        return this.compilerWorkflow;
    }

    @Override
    public ICompilerWorkflow getSimCompilerWorkflow() {
        return this.microbitSimCompilerWorkflow;
    }

    @Override
    public String getProgramToolboxBeginner() {
        return this.microbitProperties.getProperty("robot.program.toolbox.beginner");
    }

    @Override
    public String getProgramToolboxExpert() {
        return this.microbitProperties.getProperty("robot.program.toolbox.expert");
    }

    @Override
    public String getProgramDefault() {
        return this.microbitProperties.getProperty("robot.program.default");
    }

    @Override
    public String getConfigurationToolbox() {
        return this.microbitProperties.getProperty("robot.configuration.toolbox");
    }

    @Override
    public String getConfigurationDefault() {
        return this.microbitProperties.getProperty("robot.configuration.default");
    }

    @Override
    public String getRealName() {
        return this.microbitProperties.getProperty("robot.real.name");
    }

    @Override
    public Boolean hasSim() {
        return this.microbitProperties.getProperty("robot.sim").equals("true") ? true : false;
    }

    @Override
    public String getInfo() {
        return this.microbitProperties.getProperty("robot.info") != null ? this.microbitProperties.getProperty("robot.info") : "#";
    }

    @Override
    public Boolean isBeta() {
        return this.microbitProperties.getProperty("robot.beta") != null ? true : false;
    }

    @Override
    public String getConnectionType() {
        return this.microbitProperties.getProperty("robot.connection");
    }

    @Override
    public RobotSimulationCheckVisitor getSimProgramCheckVisitor(Configuration brickConfiguration) {
        return new SimulationCheckVisitor(brickConfiguration);
    }

    @Override
    public RobotBrickCheckVisitor getRobotProgramCheckVisitor(Configuration brickConfiguration) {
        return null;
    }

    @Override
    public Boolean hasConfiguration() {
        return this.microbitProperties.getProperty("robot.configuration") != null ? false : true;
    }

    @Override
    public String getGroup() {
        return this.microbitProperties.getProperty("robot.group") != null ? this.microbitProperties.getProperty("robot.group") : this.name;
    }

    @Override
    public String generateCode(Configuration brickConfiguration, ArrayList<ArrayList<Phrase<Void>>> phrasesSet, boolean withWrapping) {
        // TODO Auto-generated method stub
        return null;
    }

}
