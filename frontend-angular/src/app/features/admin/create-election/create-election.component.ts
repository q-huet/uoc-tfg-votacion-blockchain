import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormArray, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ElectionService } from '../../../core/services/election.service';
import { CreateElectionRequest } from '../../../models/election.model';
import { MessageService } from 'primeng/api';

// PrimeNG
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { CalendarModule } from 'primeng/calendar';
import { ButtonModule } from 'primeng/button';
import { ToastModule } from 'primeng/toast';
import { DividerModule } from 'primeng/divider';

import { DialogModule } from 'primeng/dialog';
import { MessageModule } from 'primeng/message';

@Component({
  selector: 'app-create-election',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    CardModule,
    InputTextModule,
    InputTextareaModule,
    CalendarModule,
    ButtonModule,
    ToastModule,
    DividerModule,
    DialogModule,
    MessageModule
  ],
  providers: [MessageService],
  templateUrl: './create-election.component.html',
  styleUrls: ['./create-election.component.scss']
})
export class CreateElectionComponent {
  electionForm: FormGroup;
  loading = false;
  showPrivateKeyDialog = false;
  generatedPrivateKey = '';

  constructor(
    private fb: FormBuilder,
    private electionService: ElectionService,
    private router: Router,
    private messageService: MessageService
  ) {
    this.electionForm = this.fb.group({
      title: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(100)]],
      description: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(500)]],
      startTime: [null, Validators.required],
      endTime: [null, Validators.required],
      options: this.fb.array([])
    });

    // Add initial options
    this.addOption();
    this.addOption();
  }

  get options() {
    return this.electionForm.get('options') as FormArray;
  }

  addOption() {
    const optionGroup = this.fb.group({
      title: ['', Validators.required],
      description: ['']
    });
    this.options.push(optionGroup);
  }

  removeOption(index: number) {
    this.options.removeAt(index);
  }

  onSubmit() {
    if (this.electionForm.invalid) {
      this.electionForm.markAllAsTouched();
      return;
    }

    if (this.options.length < 2) {
      this.messageService.add({severity:'error', summary:'Error', detail:'Debe haber al menos 2 opciones'});
      return;
    }

    this.loading = true;
    const formValue = this.electionForm.value;

    // Helper to format date as Local ISO string (ignoring timezone offset for backend compatibility)
    const toLocalISO = (date: Date): string => {
      const tzOffset = date.getTimezoneOffset() * 60000;
      return new Date(date.getTime() - tzOffset).toISOString().slice(0, -1);
    };

    const request: CreateElectionRequest = {
      title: formValue.title,
      description: formValue.description,
      startTime: toLocalISO(formValue.startTime),
      endTime: toLocalISO(formValue.endTime),
      options: formValue.options.map((opt: any, index: number) => ({
        optionId: `opt-${index + 1}`,
        title: opt.title,
        description: opt.description,
        displayOrder: index + 1
      }))
    };

    this.electionService.createElection(request).subscribe({
      next: (result) => {
        this.loading = false;
        this.messageService.add({severity:'success', summary:'Éxito', detail:'Elección creada correctamente'});

        // Mostrar clave privada
        this.generatedPrivateKey = result.privateKey;
        this.showPrivateKeyDialog = true;
      },
      error: (err) => {
        this.loading = false;
        this.messageService.add({severity:'error', summary:'Error', detail: 'Error al crear la elección'});
        console.error(err);
      }
    });
  }

  closeDialog() {
    this.showPrivateKeyDialog = false;
    this.router.navigate(['/admin']);
  }
}
