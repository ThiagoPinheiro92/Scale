public class ScheduleGenerator {
	public static Schedule generateWeeklySchedule(List<Employee> employees, List<Machine> machines) {
		Schedule schedule = new Schedule();
		
		// Ordena máquinas por dificuldade
		machines.sort((m1, m2) -> Integer.compare(m2.getEffectiveDifficulty(), m1.getEffectiveDifficulty()));
		
		// Ordena funcionários por capacidade (mais capazes primeiro)
		employees.sort((e1, e2) -> Integer.compare(e2.getMaxDifficulty(), e1.getMaxDifficulty()));
		
		// Distribuição inicial
		for (Machine machine : machines) {
			for (Employee employee : employees) {
				if (canAssignMachine(employee, machine, schedule)) {
					schedule.assignMachine(employee, machine);
					break;
				}
			}
		}
		
		return schedule;
	}
	
	private static boolean canAssignMachine(Employee employee, Machine machine, Schedule schedule) {
		// Verifica se o funcionário já atingiu o limite de máquinas
		if (schedule.getMachinesByEmployee(employee).size() >= 3) {
			return false;
		}
		
		// Verifica restrições PCD
		if (employee.isPcd() && machine.getCurrentProduct() != null &&
		employee.getRestrictedProductTypes().contains(machine.getCurrentProduct().getType())) {
			return false;
		}
		
		// Verifica dificuldade máxima
		return machine.getEffectiveDifficulty() <= employee.getMaxDifficulty();
	}
}