package com.th.scala.core.services;

import com.th.scala.core.models.Machine;
import java.util.ArrayList;
import java.util.List;

public class MachineFactory {
	
	public List<Machine> createDefaultMachines() {
		List<Machine> machines = new ArrayList<>();
		
		// Máquinas com dificuldade inicial 0 (será definida via Spinner)
		// Formato: new Machine(nome, número, operador, produto, dificuldade, éPesada)
		
		// Máquinas de exemplo - nomes genéricos
		machines.add(new Machine("Torno CNC", 101, "", "Peças Automotivas", 0, false));
		machines.add(new Machine("Fresa Industrial", 102, "", "Componentes Aeroespaciais", 0, false));
		machines.add(new Machine("Retífica de Precisão", 103, "", "Moldes Industriais", 0, true));
		machines.add(new Machine("Injetora de Plástico", 104, "", "Peças Plásticas", 0, false));
		machines.add(new Machine("Cortadora Laser", 105, "", "Chapas Metálicas", 0, true));
		machines.add(new Machine("Prensa Hidráulica", 106, "", "Forjamento", 0, true));
		machines.add(new Machine("Furadeira Radial", 107, "", "Montagem", 0, false));
		machines.add(new Machine("Retífica Cilíndrica", 108, "", "Eixos", 0, false));
		machines.add(new Machine("Torno Mecânico", 109, "", "Peças Torneadas", 0, false));
		machines.add(new Machine("Serra de Fita", 110, "", "Corte de Materiais", 0, false));
		
		return machines;
	}
	
	// Método opcional para criar máquinas com dificuldade pré-definida (para testes)
	public List<Machine> createTestMachines() {
		List<Machine> machines = new ArrayList<>();
		
		// 2 máquinas difíceis
		machines.add(new Machine("Torno Pesado", 201, "", "Eixos Grandes", 3, true));
		machines.add(new Machine("Centro de Usinagem", 202, "", "Componentes Complexos", 3, true));
		
		// 3 máquinas médias
		machines.add(new Machine("Fresa CNC", 203, "", "Peças Médias", 2, false));
		machines.add(new Machine("Retífica Plana", 204, "", "Superfícies Planas", 2, false));
		machines.add(new Machine("Furadeira CNC", 205, "", "Furos Precisos", 2, false));
		
		// 5 máquinas fáceis
		machines.add(new Machine("Serra Circular", 206, "", "Corte Rápido", 1, false));
		machines.add(new Machine("Lixadeira", 207, "", "Acabamento", 1, false));
		machines.add(new Machine("Torno Manual", 208, "", "Peças Simples", 1, false));
		machines.add(new Machine("Prensa Manual", 209, "", "Montagem", 1, false));
		machines.add(new Machine("Furadeira Bancada", 210, "", "Furos Simples", 1, false));
		
		return machines;
	}
}