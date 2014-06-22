package com.sinlimites.objects;

/**
 * Created by Dennis on 14-5-2014 at 21:34)
 * <p/>
 * This code is part of the RestServiceApp project.
 * This class is within package nl.dennisvdwielen.entity
 */

public class Container {

	private String equipmentNumber;

	private Handling handlingID;

	private Long consignmentNumber;
	private Integer uno;
	private Double imo;
	private Double flashpoint;
	private Integer stowagePosition;
	private Integer quantityInContainer;
	private Long weight;
	private String portOfDischarge;
	private String terminal;

	public String getEquipmentNumber() {
		return equipmentNumber;
	}

	public void setEquipmentNumber(String equipmentNumber) {
		this.equipmentNumber = equipmentNumber;
	}

	public Handling getHandlingID() {
		return handlingID;
	}

	public void setHandlingID(Handling handlingID) {
		this.handlingID = handlingID;
	}

	public Long getConsignmentNumber() {
		return consignmentNumber;
	}

	public void setConsignmentNumber(Long consignmentNumber) {
		this.consignmentNumber = consignmentNumber;
	}

	public Integer getUno() {
		return uno;
	}

	public void setUno(Integer uno) {
		this.uno = uno;
	}

	public Double getImo() {
		return imo;
	}

	public void setImo(Double imo) {
		this.imo = imo;
	}

	public Double getFlashpoint() {
		return flashpoint;
	}

	public void setFlashpoint(Double flashpoint) {
		this.flashpoint = flashpoint;
	}

	public Integer getStowagePosition() {
		return stowagePosition;
	}

	public void setStowagePosition(Integer stowagePosition) {
		this.stowagePosition = stowagePosition;
	}

	public Integer getQuantityInContainer() {
		return quantityInContainer;
	}

	public void setQuantityInContainer(Integer quantityInContainer) {
		this.quantityInContainer = quantityInContainer;
	}

	public Long getWeight() {
		return weight;
	}

	public void setWeight(Long weight) {
		this.weight = weight;
	}

	public String getPortOfDischarge() {
		return portOfDischarge;
	}

	public void setPortOfDischarge(String portOfDischarge) {
		this.portOfDischarge = portOfDischarge;
	}

	public String getTerminal() {
		return terminal;
	}

	public void setTerminal(String terminal) {
		this.terminal = terminal;
	}
}
