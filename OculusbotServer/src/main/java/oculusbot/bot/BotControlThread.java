package oculusbot.bot;

import static oculusbot.pi.basics.Pins.GPIO_07;
import static oculusbot.pi.basics.Pins.GPIO_08;
import static oculusbot.pi.basics.Pins.GPIO_12;
import static oculusbot.pi.basics.Pins.GPIO_13;
import static oculusbot.pi.basics.Pins.GPIO_14;
import static oculusbot.pi.basics.Pins.GPIO_15;
import static oculusbot.pi.basics.Pins.GPIO_16;
import static oculusbot.pi.basics.Pins.GPIO_18;
import static oculusbot.pi.basics.Pins.GPIO_19;
import static oculusbot.pi.basics.Pins.GPIO_20;
import static oculusbot.pi.basics.Pins.GPIO_21;
import static oculusbot.pi.basics.Pins.GPIO_23;
import static oculusbot.pi.basics.Pins.GPIO_24;
import static oculusbot.pi.basics.Pins.GPIO_25;
import static oculusbot.pi.basics.Pins.GPIO_26;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketTimeoutException;

import com.pi4j.io.gpio.GpioController;

import oculusbot.network.NetworkThread;
import oculusbot.pi.motors.MotorThread;

public class BotControlThread extends NetworkThread {
	private MotorThread yaw;
	private MotorThread pitch;
	private MotorThread roll;
	private GpioController gpio;

	public void setYaw(double yaw){
		this.yaw.setTargetAngle(yaw);
	}
	
	public double getYaw(){
		return yaw.getCurrentAngle();
	}
	
	public void setPitch(double pitch){
		this.pitch.setTargetAngle(pitch);
	}
	
	public double getPitch(){
		return pitch.getCurrentAngle();
	}
	
	public void setRoll(double roll){
		this.roll.setTargetAngle(roll);
	}
	
	public double getRoll(){
		return roll.getCurrentAngle();
	}
	
	
	public BotControlThread(int port, GpioController gpio) {
		super(port);
		this.gpio = gpio;
	}
	
	private void set(double yaw, double pitch, double roll){
		setYaw(yaw);
		setPitch(pitch);
		setRoll(roll);
	}
	
	@Override
	protected void setup() {
		super.setup();
		yaw = new MotorThread(GPIO_24, GPIO_25, GPIO_08, GPIO_07, GPIO_19, gpio, 30, false);
		pitch = new MotorThread(GPIO_14, GPIO_15, GPIO_18, GPIO_23, GPIO_26, gpio, 40, false);
		roll = new MotorThread(GPIO_12, GPIO_16, GPIO_20, GPIO_21, GPIO_13, gpio, 30, true);
		yaw.start();
		pitch.start();
		roll.start();
	}
	
	@Override
	public Status getStatus() {
		return passthroughStatus(yaw, pitch, roll);
	}

	@Override
	protected void doNetworkOperation() throws IOException {
		DatagramPacket packet;
		try {
			packet = receive();
		} catch (SocketTimeoutException e) {
			return;
		}

		double[] values = convert(new String(packet.getData()).trim());
		if (values != null && values.length > 2) {
			set(values[0], values[1], values[2]);
		}
	}

	private double[] convert(String data) {
		String[] s = data.split(" ");
		double[] result = new double[s.length];
		for (int i = 0; i < s.length; i++) {
			try {
				result[i] = Double.parseDouble(s[i]);
			} catch (NumberFormatException e) {
				return null;
			}
		}
		return result;
	}

	@Override
	protected void shutdown() {
		yaw.interrupt();
		pitch.interrupt();
		roll.interrupt();
		waitForClosingThreads(yaw, pitch, roll);
	}
}
