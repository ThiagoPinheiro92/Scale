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
		machines.add(new Machine("Injetora", 905, "", "Escalonado Duplo", 0, false));
		machines.add(new Machine("Injetora", 638, "", "Corpinho 1/2 3/4", 0, false));
		machines.add(new Machine("Injetora", 639, "", "Flange 7/8", 0, true));
		machines.add(new Machine("Injetora", 648, "", "Terminal de saida", 0, false));
		machines.add(new Machine("Injetora", 644, "", "Manipulo", 0, true));
		machines.add(new Machine("Injetora", 643, "", "PMG", 0, true));
		machines.add(new Machine("Injetora", 303, "", "Junta V8", 0, false));
		machines.add(new Machine("Injetora", 408, "", "Ved cx dagua 30mm", 0, false));
		machines.add(new Machine("injetora", 407, "", "Haste", 0, false));
		machines.add(new Machine("Injetora", 520, "", "Abracadeira torn", 0, false));
		machines.add(new Machine("Injetora", 32002, "", "Boia BB ", 0, false));
		machines.add(new Machine("Injetora", 1503, "", "Corpo torneira jard", 0, false));
		machines.add(new Machine("Injetora", 1817, "", "Escalonado Simples", 0, false));
		machines.add(new Machine("Injetora", 1816, "", "Adaptador", 0, false));
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